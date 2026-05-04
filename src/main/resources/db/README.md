# SQL Scripts

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