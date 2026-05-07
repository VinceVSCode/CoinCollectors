# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.4.1] - YYYY-MM-DD - 2026-05-07 

### Added
- Controller tests for collection query endpoints.
- Controller tests for collection write endpoint.
- REST handling for invalid or missing JSON request bodies.
- `PagedResponse<T>` for frontend-friendly paginated API responses.
- Total-count query methods for owned and missing coin screens.

### Changed
- HTTP layer now has automated test coverage for core collection read/write flows.
- Collection query endpoints now return pagination metadata when `page` and `size` are provided.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A