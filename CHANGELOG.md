# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.3.3] - YYYY-MM-DD - 2026-05-07 

### Added
- Initial Spring Boot HTTP layer for collection screen queries.
- `CollectionQueryController` with owned and missing coin endpoints.
- `RestExceptionHandler` for request validation errors.
- `SetCoinQuantityRequest` and `CollectionEntryResponse` for collection write API operations.
- `CollectionCommandController` with a PUT endpoint for setting coin quantity.
- Backend-generated collection entry IDs through `getNextCollectionEntryId()`.

### Changed
- `App.java` now starts the Spring Boot application after database bootstrap.
- Collection quantity updates no longer require the frontend to provide a collection entry ID.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A