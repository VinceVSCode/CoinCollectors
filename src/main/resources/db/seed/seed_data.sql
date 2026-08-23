-- Dev-only seed accounts, password for all three is "password123". Never used in production.
-- IDs are auto-generated (users_id_seq, from V4) and are NOT predictable: they are only
-- 1/2/3 on a pristine database. users_id_seq keeps advancing across truncations (the
-- integration test suite empties `users` without resetting it, and reset_data.sql deliberately
-- doesn't restart it either), so a re-seed can land on 4/5/6 or higher. Nothing below may
-- assume a particular id — collection_entries resolves users by username instead.
-- The password_hash values are real BCrypt hashes of "password123" (not placeholders) so
-- login actually works against seeded accounts without any extra setup step.
-- Idempotent: ON CONFLICT DO NOTHING lets DatabaseBootstrap re-run this seed safely against an
-- already-seeded volume (e.g. `docker compose up`/`restart` without `down -v`) whenever
-- COIN_TRACKER_DB_SEED_ON_START=true, which docker-compose always sets, instead of crashing with
-- a duplicate-key violation on users_username_key.
INSERT INTO users (username, password_hash, role, is_active) VALUES
    ('vince', '$2a$10$VD9WWdtJLCs7NMeaZRHG1uf69vJb2bLsRutQkR/CZQz4b2e3rnvLm', 'ADMIN', TRUE),
    ('alex', '$2a$10$5HORknBKFVdRcnjEBMzBUemN/0MTLcnA53jAzZxEiQLkQfK5gVI3q', 'USER', TRUE),
    ('maria', '$2a$10$DJ7vqjwORKgmk/XqiFir2uMJLeH5OSMc/fVAgbkgGQjGWv3SpnBme', 'USER', TRUE)
ON CONFLICT (username) DO NOTHING;
INSERT INTO coins (id, country, denomination, year) VALUES
    (1, 'Bulgaria', '1 Lev', 2002),
    (2, 'Germany', '1 Euro', 2010),
    (3, 'France', '2 Euro', 2015),
    (4, 'Italy', '50 Centesimi', 2007),
    (5, 'Spain', '20 Centesimos', 2003),
    (6, 'Greece', '1 Euro', 2008)
ON CONFLICT (id) DO NOTHING;

-- A quantity of 0 (e.g. vince/coin 3 and 4, maria/coin 1) is intentional: it exercises the
-- "explicit zero counts as not-owned" behavior in the owned/missing coin queries (see
-- PostgresCollectionEntryRepository) rather than every seeded coin being either owned or
-- entirely absent.
-- user_id is looked up by username rather than hardcoded, because seeded user ids are not
-- stable (see the header comment). The JOIN also means a row whose username somehow isn't
-- present is skipped rather than failing fk_collection_entries_user at startup.
INSERT INTO collection_entries (user_id, coin_id, quantity)
SELECT u.id, v.coin_id, v.quantity
FROM (VALUES
    ('vince', 1, 2),
    ('vince', 2, 1),
    ('vince', 3, 0),
    ('vince', 4, 0),
    ('alex', 2, 3),
    ('alex', 5, 1),
    ('maria', 1, 0),
    ('maria', 6, 2)
) AS v(username, coin_id, quantity)
JOIN users u ON u.username = v.username
ON CONFLICT (user_id, coin_id) DO NOTHING;