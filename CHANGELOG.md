# Changelog
All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project follows Semantic Versioning.

## [0.1.7] - 2026-03-18 

### Added
- Overloaded `CoinCatalogService#addCoin(int id, String country, String denomination, int year)`.
- Test to verify that external list changes do not modify repository internals.
- `CoinCatalogCli` for simple command-line interaction.
- Menu options for adding, listing, and finding coins.

### Changed
- `App.java` now adds coins through raw field values instead of constructing `Coin` directly.
- Service tests now use the new `addCoin(...)` overload.
- `CoinRepository#getAllCoins()` now returns a copy of the internal list to better protect repository state.
- `App.java` now only bootstraps the application and starts the CLI.

### Removed
- N/A

### Fixed
- N/A