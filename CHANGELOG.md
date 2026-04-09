# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.2.7] - YYYY-MM-DD - 2026-04-09 

### Added
- `CachedCoinRepository` wrapper as the structural base for future caching.
- Support for `cached-memory` and `cached-postgres` repository types.
- Basic ID-based caching in `CachedCoinRepository` for `findCoinById(int id)`.
- Cache synchronization on add, update, and remove operations.
- Tests for caching behavior and cache consistency after writes.
### Changed
- `RepositoryFactory` can now create cached repository wrappers.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A