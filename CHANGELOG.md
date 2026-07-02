# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

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