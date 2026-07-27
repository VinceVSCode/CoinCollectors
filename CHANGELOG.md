# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

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