# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.3.2] - YYYY-MM-DD - 2026-05-30 

### Added
- `OwnedCoinView` for frontend-friendly owned coin query results.
- `getOwnedCoinsForUser(int userId)` in the collection entry repository and collection tracking service.
- PostgreSQL-backed integration tests for owned coin query behavior.
- `MissingCoinView` for frontend-friendly missing coin query results.
- `getMissingCoinsForUser(int userId)` in the collection entry repository and collection tracking service.
- PostgreSQL-backed integration tests for missing coin query behavior.

### Changed
- Ownership tracking now includes a joined query shaped for future UI screens.
- Ownership tracking now supports both owned and missing coin screen queries.
### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A