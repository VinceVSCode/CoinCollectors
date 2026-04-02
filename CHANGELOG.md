# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.1.8] - 2026-03-21 

### Added
- `PostgresCoinRepository` with PostgreSQL-backed `addCoin` and `getAllCoins`.
- Basic JDBC integration through `DatabaseConnection`.
### Changed
- `App.java` can now be used to manually test PostgreSQL-backed coin storage.
### Removed
- N/A

### Fixed
- N/A

### Bugs
- 'App.java' does not communicate correctly.