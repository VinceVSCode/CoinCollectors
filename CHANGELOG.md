# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.2.8] - YYYY-MM-DD - 2026-04-22 

### Added
- `CacheMode` enum for predefined cache TTL profiles.
- `CacheConfig` for loading cache mode from environment variables.

### Changed
- Cached repositories now use startup configuration instead of hardcoded TTL values.
- Cache mode can now be selected through `COIN_TRACKER_CACHE_MODE`.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A