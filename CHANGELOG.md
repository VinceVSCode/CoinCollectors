# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.2.5] - 2026-04-26 

### Added
- Repository selection through `COIN_TRACKER_REPOSITORY`.
- `RepositoryFactory` for repository creation based on environment configuration.

### Changed
- `App.java` now chooses between in-memory and PostgreSQL repository implementations at startup.
- `App.java` now delegates repository selection to `RepositoryFactory`.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A