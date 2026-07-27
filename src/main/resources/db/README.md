# SQL Scripts

> **Note:** the `schema/` folder and the top-level `001_create_coins_table.sql` /
> `002_create_users_table.sql` / `003_create_collection_entries_table.sql` files below describe
> the *original* manual-execution workflow and are no longer run by the application — schema
> setup is now handled automatically by the Flyway migrations in `db/migration/` via
> `DatabaseBootstrap`. Only `reset/` and `seed/` are still actually invoked by the app (opt-in
> via the `COIN_TRACKER_DB_RESET_ON_START` / `COIN_TRACKER_DB_SEED_ON_START` env vars). Kept
> here for historical context and manual/ad-hoc use.

## Folder structure

- `schema/` - creates or updates database structure
- `reset/` - clears existing data
- `seed/` - inserts sample/demo data

## Recommended execution order

1. `schema/001_ensure_schema.sql`
2. `reset/reset_data.sql`
3. `seed/seed_data.sql`

## Notes

- Run schema first to ensure required tables exist.
- Run reset before seed when you want a clean, predictable dataset.
- Reset deletes data only, not tables.