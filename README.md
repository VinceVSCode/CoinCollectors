# CoinCollectors
The aim of this folder is an application for coin collectors A learning project: a Java-based coin collection tracker (CLI first, then DB, caching, API, UI). That is the main idea. Change will be done along the way. Inside the code, the project will be named "cointracker".

## Requirements
- Java 21+
- Maven 3.9+

## Repository selection

The application chooses the repository implementation using the environment variable:

- `COIN_TRACKER_REPOSITORY`

Supported values:
- `memory`
- `postgres`

Example:

## ENV VAR
COIN_TRACKER_REPOSITORY=memory or postgres
COIN_TRACKER_URL = YOUR_URL
COIN_TRACKER_USERNAME = YOUR_USERNAME
COIN_TRACKER_PASSWORD = YOUR_PASSWORD

## Cache configuration

Cached repository modes can use a predefined cache TTL mode through:

- `COIN_TRACKER_CACHE_MODE`

Supported values:
- `short` → 15 seconds
- `medium` → 60 seconds
- `long` → 600 seconds

Example:

```text
COIN_TRACKER_REPOSITORY=cached-postgres
COIN_TRACKER_CACHE_MODE=medium

## Run
```bash
mvn -q compile exec:java -Dexec.mainClass="com.vincevscode.cointracker.App"

