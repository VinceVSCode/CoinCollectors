# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

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