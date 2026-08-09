# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.11.0] - YYYY-MM-DD - 2026-08-09

Brute-force throttling on the login endpoint. The pen-test pass in 0.7.2 confirmed login does
not leak whether an account exists, but nothing limited how fast guesses could be made.

### Added
- `LoginRateLimiter`: counts recent failed logins per username **and** per client address,
  refusing further attempts once either crosses 10 within a rolling 15-minute window. Both keys
  are tracked deliberately — throttling only by address does nothing against credential stuffing
  spread across hosts, while throttling only by username lets an attacker deny service to any
  account they can name. Windows expire on their own, so a targeted account is temporarily
  slowed rather than locked out pending admin action.
- `TooManyLoginAttemptsException` and a `RestExceptionHandler` mapping to **429** with a
  `Retry-After` header. Kept distinct from the 401: answering "invalid username or password"
  would be untrue (nothing was verified) and would leave a legitimate user unable to tell a
  wrong password from a temporary lockout.
- `LoginRateLimiterTest` (12 cases, driven by an injectable `Clock` so window expiry is tested
  without sleeping) plus two endpoint-level cases in `AuthControllerSecurityTest`.
- `AuthTestSupport.fromAddress(...)` for pinning a request's client address in tests.

### Changed
- `AuthController#login` consults the limiter **before** calling `authenticate()`, so a
  throttled attempt costs no BCrypt verification — precisely the expensive work a brute-force
  attempt is trying to inflict. A successful login clears the username's budget but deliberately
  leaves the address budget intact, so an attacker holding one valid account cannot reset their
  own address allowance between guesses.
- Existing login tests now pin distinct client addresses. The limiter is a singleton and Spring
  caches one context across slice tests with matching configuration, so tests sharing the
  default 127.0.0.1 would have spent each other's budget and failed depending on order.

### Removed
- N/A

### Fixed
- N/A

### Security notes
- Failures are counted for unknown usernames exactly as for real ones. Skipping the unknown case
  would make "throttled or not" a reliable oracle for whether an account exists, undoing the
  generic-error-message protection login already relies on. Covered by a regression test.
- Usernames are lower-cased for keying, so alternating capitalisation cannot buy a fresh budget.
- The client address comes from `getRemoteAddr()` and never `X-Forwarded-For`, which is
  client-supplied and would let an attacker mint a new budget per request. Putting this app
  behind a reverse proxy requires configuring the proxy as a trusted source first.
- State is in-memory and per-instance, which suits the single-container deployment in
  `docker-compose.yml`. Running multiple replicas would give each its own budget and multiply
  the effective limit by the replica count — that is the point at which this needs shared
  storage.

### Bugs
- N/A

## [0.10.0] - YYYY-MM-DD - 2026-08-03

Audit trail for privileged actions. Role changes, activations/deactivations and every catalog
mutation are now recorded with who did them, when, and what changed.

### Added
- `V6__admin_actions.sql` creating the append-only `admin_actions` table. Neither the actor nor
  the target is a foreign key, and the actor's username is denormalized alongside their id: an
  audit record has to outlive the account and the row that produced it, and the most interesting
  thing to audit is a deletion — an FK would delete the evidence along with it.
- `AdminAuditRepositoryInterface` + `PostgresAdminAuditRepository`. `created_at` comes from the
  column's `DEFAULT NOW()` so timestamps use the database clock rather than a possibly-skewed
  app container's. Reads return `action` as the raw stored string, so a record written by a
  build that knew an action this one doesn't still lists instead of failing.
- `AdminActionType` enum, `AdminActor` value type, `AdminActionView`, and `AdminActorFactory`
  (the single place `Authentication` is unwrapped for auditing).
- `AdminAuditService` (read side only) and `AdminAuditController` exposing
  `GET /api/admin/audit?limit=` (ADMIN-only). Out-of-range limits are clamped, not rejected;
  the default is 50 and the maximum 500.
- An Audit Trail section on `admin.html`, refreshed after every mutation.
- `AdminAuditServiceTest` (4), `AdminAuditControllerTest` (5), and new audit assertions across
  `UserManagementServiceTest` and `CoinCatalogManagementServiceTest`.

### Changed
- `UserManagementService` and `CoinCatalogManagementService` now take an `AdminActor` as their
  first parameter on every mutating method, and record the audit entry inside the existing
  `@Transactional` method. Passing the actor explicitly (rather than having services read
  `SecurityContextHolder`) keeps the service layer free of Spring Security and makes it
  impossible to invoke an audited operation without saying who is responsible. Writing inside
  the same transaction means a change and its audit record commit together or not at all — an
  audit trail that can silently miss entries proves nothing.
- Forced coin deletion now counts the affected collection entries before deleting. It previously
  skipped the count as an optimization when `force=true`; the count is now always taken so the
  record can state how many entries cascaded away, which is the most important consequence of
  that action.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A

## [0.9.0] - YYYY-MM-DD - 2026-07-31

Admin coin catalog management. Until now the catalog could only be changed by editing
`seed_data.sql` and resetting the database.

### Added
- `AdminCoinController` (`hasRole('ADMIN')`): `POST /api/admin/coins`,
  `PUT /api/admin/coins/{coinId}`, `DELETE /api/admin/coins/{coinId}`. Reads stay on the
  existing `GET /api/coins`, so there is deliberately no admin-only list endpoint.
- `CoinCatalogManagementService` with input validation (required non-blank country/denomination,
  trimmed, max 100 characters; year required and between 1 and 2999) and the delete cascade
  guard described below.
- `CoinCatalogCommandRepositoryInterface` + `PostgresCoinCatalogCommandRepository` — the write
  half of the live catalog path, mirroring the query/command split already used by the
  collection controllers. Kept separate from the older unwired `CoinRepositoryInterface`.
- `CoinRequest` DTO. It has no `id` field, so a client can't reassign a coin's id via the body —
  the id comes from the path on update and the sequence on create.
- `V5__coins_id_generated.sql`, retrofitting a sequence default onto `coins.id` (same pattern as
  V2 for `collection_entries` and V4 for `users`). Required because `coins.id` was a plain
  `INTEGER PRIMARY KEY` with no default — only `seed_data.sql` ever supplied ids.
- A Coin Catalog section on `admin.html`: add/edit form and a table with per-row Edit and Delete.
- `CoinCatalogManagementServiceTest` (12 cases) and `AdminCoinControllerTest` (12 cases).

### Changed
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security notes
- `collection_entries.coin_id` is `ON DELETE CASCADE`, so deleting a coin that users own does
  not fail — it silently destroys their collection entries and shifts their progress. Deleting
  a referenced coin is therefore refused unless the caller passes `?force=true`, and the
  refusal message reports exactly how many entries would be lost. The admin UI performs the
  unqualified delete first and only asks for confirmation using the server-reported count, so
  the number shown is the number that will actually be deleted.

### Bugs
- N/A

## [0.8.0] - YYYY-MM-DD - 2026-07-31

Collection-screen usability pass. Covers the three UX patches delivered together; the first
two are frontend-only and shipped without their own version bump.

### Added
- Sortable columns on the collection table: each header is a button that sorts ascending, then
  toggles direction, with an arrow marking the active column. Sorting happens client-side over
  the already-loaded catalog and composes with the existing search box and filter tabs.
- `js/ui.js` exposing a `CoinUI` namespace (`toast`/`success`/`error`/`info`). Toasts stack in a
  fixed bottom-right container, auto-dismiss (errors linger longer than successes), can be
  closed manually, and carry `role=alert`/`aria-live=assertive` for errors versus
  `role=status`/`polite` otherwise.
- `GET /api/users/{userId}/collection/export` (self-or-admin, same rule as the other collection
  endpoints) returning the user's owned coins as a `text/csv` attachment, plus an "Export CSV"
  button on the collection page. Deliberately unfiltered and unpaged — an export is a snapshot
  of the whole collection.
- `CollectionCsvFormatter` with RFC 4180 escaping (quoting fields containing commas/quotes/
  newlines, doubling embedded quotes) and CSV-injection neutralization: values starting with
  `= + - @` are apostrophe-prefixed so a spreadsheet reads them as text rather than a formula.
- `CollectionCsvFormatterTest` plus owner/admin/other-user/unauthenticated cases for the new
  endpoint in `CollectionQueryControllerTest`.

### Changed
- `index.html` and `admin.html` replace their inline `.status` divs with toasts, so feedback is
  visible regardless of scroll position (a save confirmation below a long coin table could
  previously land off-screen). `login.html` and `register.html` intentionally keep inline
  status: on a single-form page a corner toast is easy to miss, and credential errors belong
  next to the form.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A

## [0.7.4] - YYYY-MM-DD - 2026-07-27

### Added
- N/A

### Changed
- N/A

### Removed
- N/A

### Fixed
- `seed_data.sql` is now idempotent (`ON CONFLICT DO NOTHING` on the users/coins/collection_entries inserts), so the app no longer crashes on startup when `COIN_TRACKER_DB_SEED_ON_START=true` re-runs the seed against an already-populated volume (e.g. `docker compose up`/`restart` without `down -v`). Previously the re-seed failed with `duplicate key value violates unique constraint "users_username_key"`.

### Bugs
- N/A

## [0.7.3] - YYYY-MM-DD - 2026-07-08

### Added
- `AccountStatusFilter`: authenticated API requests are re-checked against the user's current active status, so an admin deactivation takes effect on the user's very next request instead of only at session expiry. The filter blocks only on a positive "inactive" finding (a missing lookup falls through), skips static pages and the login/register/logout endpoints, and returns a 401 `{"error":"Your account has been deactivated."}`.
- `AccountStatusFilterTest` covering deactivated/active/unknown/unauthenticated principals and the skipped paths.

### Changed
- `SecurityConfig` registers the new filter after the CSRF cookie filter.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A

## [0.7.2] - YYYY-MM-DD - 2026-07-07

### Added
- Adversarial/penetration test suites: `CollectionTrackingServiceSecurityTest`, `UserRegistrationServiceSecurityTest`, and `AuthControllerSecurityTest` — covering the fixed foreign-key case, privilege-escalation/mass-assignment attempts, credential-error non-enumeration, and CSRF enforcement.

### Changed
- N/A

### Removed
- N/A

### Fixed
- `PUT /api/users/{userId}/collection/{coinId}` with a non-existent `coinId` returned a raw 500 (unhandled foreign-key `DataIntegrityViolationException` leaking the internal error and path); it now returns a clean 400 `{"error":"Coin was not found."}`.

### Security notes (verified, no change needed)
- Registration hardcodes `UserRole.USER`; injected `role`/`id`/`active` fields in the request body are unbound and ignored (no privilege escalation via mass assignment).
- Login returns an identical generic message for wrong password and unknown username (no user enumeration).
- All SQL uses parameterized `JdbcTemplate` queries; an injection payload in the username is treated as a literal.
- Over-length passwords (>72 bytes, BCrypt's limit) are rejected with a 400 rather than silently truncated.

### Bugs
- N/A

## [0.7.1] - YYYY-MM-DD - 2026-07-07

### Added
- Admin user management: `AdminUserController` (`hasRole('ADMIN')`) with `GET /api/admin/users`, `PATCH /api/admin/users/{userId}/role`, and `PATCH /api/admin/users/{userId}/active`, backed by a new `UserManagementService` and `AdminUserView`.
- `AuthUserRepositoryInterface` write/query operations: `getAllAuthUsers`, `updateRole`, `updateActive`, and `countActiveAdmins`.
- A last-active-admin safety guard: demoting or deactivating the only remaining active admin is rejected with a 400.
- `admin.html` management page (list users, promote/demote, activate/deactivate) plus an "Admin" link shown to admins on the main page.
- Tests for the service (incl. the guard), the controller (admin/non-admin/unauthenticated + CSRF), and the new repository methods.

### Changed
- `PostgresAuthUserRepository` now shares a single row mapper across its query methods.
- `SecurityConfig` permits the new `/admin.html` page.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A

## [0.7.0] - YYYY-MM-DD - 2026-07-07

### Added
- `GET /api/users/{userId}/progress` (self-or-admin) returning a new `CollectionProgressView` (userId, totalCoinsInCatalog, ownedCoinCount, missingCoinCount, percentageComplete).
- `CollectionTrackingService.getCollectionProgress(userId)`, reusing the existing owned/missing count methods and guarding against a divide-by-zero on an empty catalog (percentage rounded to one decimal).
- Tests: `CollectionProgressViewTest`, a mock-based `CollectionTrackingServiceProgressTest` (empty catalog, full, partial rounding, invalid id), and progress owner/forbidden/unauthenticated cases in `CollectionQueryControllerTest`.

### Changed
- The frontend Progress bar now reads the authoritative `/progress` endpoint instead of computing the percentage client-side.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A

## [0.6.2] - YYYY-MM-DD - 2026-07-07

### Added
- `login.html` and `register.html` pages, plus a shared `js/auth.js` (session-aware `fetchJson` with automatic CSRF header + `getMe`/`logout`) and a shared `css/app.css`.
- A "Progress" section on the main page showing owned-vs-catalog completion as a bar and percentage (computed client-side from owned/missing counts).

### Changed
- `index.html` reworked to be session-aware: removed the free-pick user dropdown; the page now loads the logged-in user via `GET /api/auth/me`, operates only on that user's collection, pre-fills catalog quantity inputs from what they own, shows a logout button, and redirects to `login.html` on any 401.
- `SecurityConfig` permits the new `/login.html` and `/register.html` pages.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A

## [0.6.1] - YYYY-MM-DD - 2026-07-07

### Added
- Method-level authorization (`@EnableMethodSecurity` + `@PreAuthorize`): collection read/write endpoints are now restricted to the owning user or an ADMIN; `GET /api/users` is ADMIN-only and `GET /api/users/{userId}` is self-or-admin.
- Owner/admin/forbidden/unauthenticated test coverage across all four data controllers, plus an `AuthTestSupport` helper for authenticating MockMvc requests as a specific `AuthUser` principal.

### Changed
- Controller tests now run against the real `SecurityConfig` filter chain (replacing the temporary `@AutoConfigureMockMvc(addFilters = false)` workaround introduced in v0.5.1).

### Removed
- N/A

### Fixed
- `CollectionQueryControllerTest.getOwnedCoinsForUser_shouldAcceptFilterSortAndPagingParameters` asserted a bare-array shape while the endpoint returns a `PagedResponse` wrapper when paging params are supplied; the assertion now matches the wrapper.

### Bugs
- N/A

## [0.6.0] - YYYY-MM-DD - 2026-07-07

### Added
- `POST /api/auth/register` (public self-registration), `POST /api/auth/login`, `POST /api/auth/logout`, and `GET /api/auth/me`, backed by a new `AuthController`.
- `UserRegistrationService` (validates username/password, hashes with BCrypt, defaults new accounts to USER + active) and `AuthUserRepositoryInterface.createAuthUser(...)`.
- `RegisterRequest`, `LoginRequest`, and `AuthResponse` DTOs.
- `V4__users_id_generated.sql`: auto-generates `users.id` via a sequence, so registration no longer needs to supply an id.
- Session-cookie authentication: every route now requires authentication except register/login and static assets; unauthenticated/forbidden requests return JSON via `JsonAuthenticationEntryPoint`/`JsonAccessDeniedHandler`.
- CSRF protection via a cookie token (`CookieCsrfTokenRepository` + `SpaCsrfTokenRequestHandler` + `CsrfCookieFilter`) suited to the JS frontend.

### Changed
- `RestExceptionHandler` maps `AuthenticationException` to 401.
- Seed data no longer hard-codes user ids (relies on the new sequence); insertion order still yields vince=1, alex=2, maria=3.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A

## [0.5.1] - YYYY-MM-DD - 2026-07-03

### Added
- `spring-boot-starter-security` and `spring-security-test` dependencies.
- `security/AuthUserDetails` and `security/AuthUserDetailsService`, adapting the existing `AuthUser`/`AuthUserQueryService` to Spring Security's `UserDetails`/`UserDetailsService` contracts.
- `config/SecurityConfig`: `PasswordEncoder` (BCrypt) and `DaoAuthenticationProvider` beans, plus a `SecurityFilterChain` that permits all requests for now (no route enforcement yet — that lands with login in a later phase).
- Real BCrypt password hashes for the 3 seed users (dev-only password `password123`), replacing the previous `NULL` placeholders.
- Tests for `AuthUserDetails` and `AuthUserDetailsService`.

### Changed
- `CoinCatalogControllerTest`, `CollectionCommandControllerTest`, `CollectionQueryControllerTest`, and `UserControllerTest` now disable the security filter chain (`@AutoConfigureMockMvc(addFilters = false)`), since Spring Security auto-configuration otherwise locks down `@WebMvcTest` slices by default; these tests exercise controller/business logic, not auth.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A

## [0.5.0] - YYYY-MM-DD - 2026-07-02

### Added
- `Dockerfile` (multi-stage Maven build + slim JRE runtime) and `.dockerignore`.
- `docker-compose.yml` with a Postgres service and the app wired to it via the existing `COIN_TRACKER_DB_*` env vars, ready for local dev with `COIN_TRACKER_DB_SEED_ON_START`.
- `.github/workflows/ci.yml`: runs `mvn -B verify` against a Postgres service container on push/PR.

### Changed
- N/A

### Removed
- N/A

### Fixed
- Bound the `spring-boot-maven-plugin` `repackage` goal to the `package` phase so `mvn package` produces a runnable executable jar (previously produced a plain jar with no main manifest attribute, since the project doesn't inherit from `spring-boot-starter-parent`).

### Bugs
- N/A

## [0.4.4] - YYYY-MM-DD - 2026-07-02

### Added
- Tests for `AuthUserQueryService` and `PostgresAuthUserRepository`.

### Changed
- N/A

### Removed
- Duplicate nested `AuthUserRepositoryInterface`/`PostgresAuthUserRepository` definitions accidentally left inside `UserRepositoryInterface` and `UserQueryRepositoryInterface`.

### Fixed
- `AuthUserQueryService` now compiles against the top-level `AuthUserRepositoryInterface` (the same one `ApplicationConfiguration` wires in), instead of an unrelated nested interface of the same name that never matched the configured bean.

### Bugs
- N/A

## [0.4.3] - YYYY-MM-DD - 2026-05-16 

### Added
- `V3__users_auth_groundwork.sql` for auth-ready user schema fields.
- `UserRole` enum with `USER` and `ADMIN`.
- `AuthUser` model for internal authentication/authorization use.
- Auth-focused user repository and service for loading users by id or username.

### Changed
- Seed data now includes role and active status for users.
- Existing user query endpoints remain unchanged while auth groundwork is added underneath.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A