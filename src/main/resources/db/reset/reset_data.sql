-- Run by DatabaseBootstrap when COIN_TRACKER_DB_RESET_ON_START=true, always right before an
-- optional seed load. Deletion order matters: collection_entries first because it FKs to both
-- users and coins (ON DELETE CASCADE would handle this anyway, but being explicit avoids
-- relying on cascade order). Note: only collection_entries_id_seq is reset here — users_id_seq
-- is NOT restarted, so a reset+reseed after some registrations gives the seed accounts fresh,
-- higher ids. That's fine: seed_data.sql resolves users by username, not by id.
DELETE FROM collection_entries;
DELETE FROM users;
DELETE FROM coins;

ALTER SEQUENCE IF EXISTS collection_entries_id_seq RESTART WITH 1;