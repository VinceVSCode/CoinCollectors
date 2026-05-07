# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.4.0] - YYYY-MM-DD - 2026-05-07 

### Added
- Database-generated IDs for `collection_entries` through PostgreSQL sequence-backed defaults.
- Spring-managed JDBC configuration for transactional collection write operations.

### Changed
- Collection entry creation no longer uses manual ID generation.
- `CollectionTrackingService` now uses transactional write methods and returns the server-controlled persisted entry.
- Collection write API no longer depends on backend-generated IDs from the client side.

### Removed
- N/A

### Fixed
- N/A

### Bugs
- N/A