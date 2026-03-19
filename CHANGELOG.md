# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.1.7] - 2026-03-18 

### Added
- `CoinRepositoryInterface` as the repository contract for coin storage operations.

### Changed
- Renamed the concrete repository to `InMemoryCoinRepository`.
- `CoinCatalogService` now depends on a repository interface instead of a concrete repository class.
- Tests were updated to use `InMemoryCoinRepository`.
### Removed
- N/A

### Fixed
- N/A