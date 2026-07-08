# CoinCollectors — Project Requirements

_Status as of v0.7.3 (2026-07-08). This document describes what the application is required to do and what has been built so far. It is a living specification, updated as the project evolves._

## 1. Purpose & Vision

CoinCollectors ("cointracker" internally) is a web application that lets people track their personal coin collections against a shared coin catalog. Each user has an account, records how many of each catalogued coin they own, and can see their collection completion progress. Administrators manage the user base.

Originally a CLI learning project, it has grown into a Spring Boot REST API with a browser front-end backed by PostgreSQL.

## 2. Scope

**In scope (built):**
- Account-based access: self-service registration, login, logout, session management.
- A shared, read-only coin catalog (seeded reference data).
- Per-user collections: set/update owned quantity per coin; view owned vs. missing coins.
- Collection progress reporting (owned vs. catalog total, percentage).
- Role-based authorization (USER, ADMIN) with per-user data ownership.
- Admin user management: list users, change roles, activate/deactivate accounts.
- A browser UI (login, register, collection dashboard, admin panel).

**Out of scope (for now):**
- Adding/editing catalog coins through the API/UI (catalog is seeded only).
- Password reset / email verification / account recovery.
- Multi-currency valuation, images, trading, or social features.
- Horizontal scaling / distributed session store (single-instance, in-memory HTTP sessions).

## 3. Users & Roles

- **USER** — a collector. Can manage only their own collection and view their own data.
- **ADMIN** — a USER plus: can list all users, change any user's role, and activate/deactivate accounts. Can also read any user's collection data.

New self-registered accounts are always created as **USER**; role is never client-assignable.

## 4. Functional Requirements

### 4.1 Authentication & Session
- FR-1: A visitor can register with a username and password (min 8 characters); usernames are unique.
- FR-2: A registered user can log in and receive an authenticated session (cookie-based).
- FR-3: A user can log out, which invalidates their session.
- FR-4: The app exposes the current session identity (`/api/auth/me`) so the UI can render session-aware views.
- FR-5: Login failures return a single generic message (no distinction between "unknown user" and "wrong password").

### 4.2 Catalog
- FR-6: Any authenticated user can browse the coin catalog, with optional filtering (country, denomination, year range), sorting, and pagination.

### 4.3 Collection & Progress
- FR-7: A user can set/update the quantity owned for a given coin in their own collection (quantity ≥ 0).
- FR-8: A user can view their **owned** coins (quantity > 0) and **missing** coins, with filtering/sorting/pagination.
- FR-9: A user can view their **progress**: total catalog size, owned count, missing count, and completion percentage (one decimal).

### 4.4 Authorization
- FR-10: A user may only read or modify their own collection and progress; accessing another user's data is forbidden (403). ADMIN may access any user's data.
- FR-11: The full user list is ADMIN-only; a single-user lookup is self-or-admin.

### 4.5 Admin Management
- FR-12: An ADMIN can list all accounts with role and active status.
- FR-13: An ADMIN can change a user's role and activate/deactivate an account.
- FR-14: The system must not allow removing or deactivating the **last active administrator**.
- FR-15: Deactivating an account revokes access on the account's **next request**, not only at session expiry.

## 5. Non-Functional Requirements

- NFR-1 (Security): Passwords are stored only as BCrypt hashes; never returned by any endpoint.
- NFR-2 (Security): All state-changing requests require a CSRF token (cookie-to-header pattern).
- NFR-3 (Security): All database access uses parameterized queries (no string-built SQL).
- NFR-4 (Robustness): Invalid input and not-found references return clean 4xx JSON errors, never a raw 500 / stack trace.
- NFR-5 (Consistency): Errors share a JSON shape `{"error": "..."}`.
- NFR-6 (Portability): Runs via Docker Compose (app + PostgreSQL) or locally with Maven + a reachable PostgreSQL.
- NFR-7 (Tech constraints): Java 21, Spring Boot 3.5.x, PostgreSQL, Flyway migrations, JdbcTemplate (no JPA), vanilla-JS front-end (no build step).
- NFR-8 (Quality): Each feature ships with unit/integration tests; security-sensitive behavior has adversarial tests.

## 6. Architecture & Tech Stack

- **Language/Runtime:** Java 21
- **Framework:** Spring Boot 3.5.11 (Web, Security, JDBC)
- **Database:** PostgreSQL, schema managed by Flyway migrations (`db/migration`)
- **Data access:** `JdbcTemplate`, repository-interface + `Postgres*` implementation pattern
- **Layering:** `model` → `repository` → `service` (validation, `@Transactional`) → `api` (controllers) → `view`/`dto`
- **Auth:** Spring Security, session cookies, BCrypt, cookie-based CSRF, method-level `@PreAuthorize`
- **Front-end:** static HTML/CSS/vanilla-JS served by Spring Boot (`login`, `register`, `index`, `admin`)
- **Build:** Maven (executable Spring Boot jar), multi-stage Dockerfile, `docker-compose.yml`
- **CI:** GitHub Actions runs `mvn -B verify` against a PostgreSQL service container

### 6.1 Data Model (tables)
- `coins` — id, country, denomination, year
- `users` — id, username (unique), password_hash, role, is_active, created_at
- `collection_entries` — id, user_id → users, coin_id → coins, quantity; unique (user_id, coin_id)

### 6.2 Configuration (environment variables — web app)
- `COIN_TRACKER_DB_URL`, `COIN_TRACKER_DB_USERNAME`, `COIN_TRACKER_DB_PASSWORD` — required datasource config.
- `COIN_TRACKER_DB_SEED_ON_START` — `true` to load seed data on startup.
- `COIN_TRACKER_DB_RESET_ON_START` — `true` to reset data on startup.
- (`COIN_TRACKER_REPOSITORY`, `COIN_TRACKER_CACHE_MODE` are legacy CLI-only settings, not used by the web app.)

## 7. API Surface (current)

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | public | Register a new USER account |
| POST | `/api/auth/login` | public | Start a session |
| POST | `/api/auth/logout` | authenticated | End the session |
| GET | `/api/auth/me` | authenticated | Current session identity |
| GET | `/api/coins` | authenticated | Browse catalog (filter/sort/page) |
| GET | `/api/users` | ADMIN | List all users |
| GET | `/api/users/{userId}` | self or ADMIN | Get one user |
| GET | `/api/users/{userId}/owned-coins` | self or ADMIN | Owned coins |
| GET | `/api/users/{userId}/missing-coins` | self or ADMIN | Missing coins |
| GET | `/api/users/{userId}/progress` | self or ADMIN | Collection progress summary |
| PUT | `/api/users/{userId}/collection/{coinId}` | self or ADMIN | Set owned quantity |
| GET | `/api/admin/users` | ADMIN | List users with role/active |
| PATCH | `/api/admin/users/{userId}/role` | ADMIN | Change a user's role |
| PATCH | `/api/admin/users/{userId}/active` | ADMIN | Activate/deactivate a user |

## 8. Delivery Status (by version)

- **0.4.4** — Fixed auth-groundwork compile bug; first auth tests.
- **0.5.0** — Dockerfile, docker-compose, GitHub Actions CI; executable-jar packaging fix.
- **0.5.1** — Spring Security plumbing (BCrypt, UserDetailsService); no enforcement yet.
- **0.6.0** — Registration, login, logout, session identity; route protection + CSRF.
- **0.6.1** — Ownership/role authorization on all data endpoints.
- **0.6.2** — Session-aware front-end (login/register pages, per-user dashboard).
- **0.7.0** — Collection progress endpoint + view; front-end progress bar.
- **0.7.1** — Admin user management (list, change role, activate/deactivate) + admin panel.
- **0.7.2** — Security/penetration pass: fixed a 500 on non-existent coin; adversarial test suites.
- **0.7.3** — Immediate access revocation on account deactivation.

## 9. Known Gaps / Future Work

- Role changes take effect on the user's next login (only *deactivation* is currently immediate).
- The seed script is not idempotent, so `docker compose up` without `down -v` can fail on a persisted volume (fix in progress).
- No catalog management, password reset, or rate limiting/lockout on login yet.
