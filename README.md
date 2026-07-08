# CoinCollectors

A web application for tracking personal coin collections. Each user has an account, records how many of each catalogued coin they own, and can see how close they are to completing the catalog. Administrators manage the user base.

Built as a Spring Boot REST API with a plain HTML/JS front-end, backed by PostgreSQL. (Internally the project is named `cointracker`.)

## Features

- **Accounts** — self-service registration, login/logout, session-cookie authentication.
- **Coin catalog** — browse the shared catalog with filtering, sorting, and pagination.
- **Personal collection** — set how many of each coin you own; see your owned and missing coins.
- **Progress** — completion percentage of your collection against the full catalog.
- **Roles & ownership** — you can only see and edit your own collection; admins can manage everyone.
- **Admin panel** — list users, change roles, activate/deactivate accounts (deactivation revokes access immediately).

## Tech stack

- Java 21, Spring Boot 3.5.x (Web, Security, JDBC)
- PostgreSQL with Flyway migrations
- Vanilla HTML/CSS/JS front-end (no build step), served by the app
- Maven build; Docker Compose for local dev; GitHub Actions CI

## Requirements

- **Docker + Docker Compose** (recommended — runs the app and database together), **or**
- **Java 21+** and **Maven 3.9+** plus a reachable **PostgreSQL** instance.

## Running the app

### Option A — Docker Compose (recommended)

```bash
docker compose up --build
```

This starts PostgreSQL and the app together and seeds demo data. Open **http://localhost:8080** — you'll be sent to the login page.

To stop and wipe the database volume (do this before re-running to avoid duplicate-seed errors):

```bash
docker compose down -v
```

> Note: the seed step is not yet idempotent, so re-running `up` against an existing database volume can fail. Use `down -v` first for a clean start.

### Option B — Maven (bring your own PostgreSQL)

Set the datasource environment variables, then run:

```bash
export COIN_TRACKER_DB_URL="jdbc:postgresql://localhost:5432/cointracker"
export COIN_TRACKER_DB_USERNAME="cointracker"
export COIN_TRACKER_DB_PASSWORD="cointracker"
export COIN_TRACKER_DB_SEED_ON_START=true   # optional: load demo data

mvn spring-boot:run
```

The app listens on port **8080**.

### Configuration (environment variables)

| Variable | Required | Purpose |
|---|---|---|
| `COIN_TRACKER_DB_URL` | yes | JDBC URL of the PostgreSQL database |
| `COIN_TRACKER_DB_USERNAME` | yes | Database username |
| `COIN_TRACKER_DB_PASSWORD` | yes | Database password |
| `COIN_TRACKER_DB_SEED_ON_START` | no | `true` to load demo seed data on startup |
| `COIN_TRACKER_DB_RESET_ON_START` | no | `true` to reset data on startup |

Flyway applies the schema migrations automatically at startup.

## Using the application

### Demo accounts (when seeded)

| Username | Password | Role |
|---|---|---|
| `vince` | `password123` | ADMIN |
| `alex` | `password123` | USER |
| `maria` | `password123` | USER |

These are development-only credentials.

### As a collector

1. Go to **http://localhost:8080**. Register a new account, or log in with a demo account.
2. On your dashboard you'll see:
   - **Progress** — a bar showing how much of the catalog you've collected.
   - **Coin Catalog** — every coin; enter a quantity and click **Save** to record how many you own.
   - **Owned Coins** / **Missing Coins** — update automatically as you save quantities.
3. Click **Log out** when done. You only ever see and edit *your own* collection.

### As an administrator

Log in as an admin (e.g. `vince`) and open **http://localhost:8080/admin.html** to:
- list all users with their role and active status,
- promote/demote users between USER and ADMIN,
- activate or deactivate accounts (a deactivated user loses access on their next request).

The system prevents removing or deactivating the last active administrator.

## API overview

All endpoints return JSON. Auth is session-cookie based; state-changing requests require the CSRF token from the `XSRF-TOKEN` cookie sent back as the `X-XSRF-TOKEN` header (the front-end does this automatically).

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/logout` | authenticated |
| GET | `/api/auth/me` | authenticated |
| GET | `/api/coins` | authenticated |
| GET | `/api/users` | admin |
| GET | `/api/users/{userId}` | self or admin |
| GET | `/api/users/{userId}/owned-coins` | self or admin |
| GET | `/api/users/{userId}/missing-coins` | self or admin |
| GET | `/api/users/{userId}/progress` | self or admin |
| PUT | `/api/users/{userId}/collection/{coinId}` | self or admin |
| GET | `/api/admin/users` | admin |
| PATCH | `/api/admin/users/{userId}/role` | admin |
| PATCH | `/api/admin/users/{userId}/active` | admin |

Query endpoints such as `/api/coins` and the collection lists accept optional `country`, `denomination`, `minYear`, `maxYear`, `sortField`, `sortDirection`, `page`, and `size` parameters; when `page` and `size` are supplied, results come back as a paged response.

## Testing

Run the test suite (unit tests run anywhere; the PostgreSQL integration tests need the datasource variables set and a reachable database):

```bash
mvn test
```

CI runs `mvn -B verify` against a PostgreSQL service container on every push and pull request.

## Project documentation

See [`.claude/plans/project-requirements.md`](.claude/plans/project-requirements.md) for the full requirements specification and delivery status.
