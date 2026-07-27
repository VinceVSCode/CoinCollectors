-- Adds the columns AuthUser/AuthUserRepositoryInterface need on top of the plain id+username
-- from V1 — this is the schema half of "auth groundwork was scaffolded but never wired up"
-- (see the project's auth roadmap). NOT NULL DEFAULT on role/is_active/created_at means
-- existing rows (if any) get sane values automatically rather than failing the ALTER.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash TEXT;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role TEXT NOT NULL DEFAULT 'USER';

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Belt-and-suspenders backfill: the DEFAULT clauses above only apply to future inserts and
-- to existing rows AT THE MOMENT the column is added (Postgres backfills defaults for
-- ADD COLUMN NOT NULL DEFAULT), so in practice these UPDATEs are no-ops — kept in case this
-- migration is ever adapted to a path where the NOT NULL constraint was added separately.
UPDATE users
SET role = 'USER'
WHERE role IS NULL;

UPDATE users
SET is_active = TRUE
WHERE is_active IS NULL;