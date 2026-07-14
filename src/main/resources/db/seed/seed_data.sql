-- Dev-only seed accounts, password for all three is "password123". Never used in production.
-- IDs are auto-generated (users_id_seq, from V4) in insertion order: vince=1, alex=2, maria=3.
-- The password_hash values are real BCrypt hashes of "password123" (not placeholders) so
-- login actually works against seeded accounts without any extra setup step.
-- KNOWN GOTCHA: these INSERTs are plain (no ON CONFLICT DO NOTHING), so re-running this script
-- against a database that already has the seed data throws a duplicate-key violation on
-- users_username_key — DatabaseBootstrap runs this unconditionally whenever
-- COIN_TRACKER_DB_SEED_ON_START=true, which docker-compose always sets, so `docker compose up`
-- without a prior `down -v` will crash on startup. Always reset the volume first.
INSERT INTO users (username, password_hash, role, is_active) VALUES
    ('vince', '$2a$10$VD9WWdtJLCs7NMeaZRHG1uf69vJb2bLsRutQkR/CZQz4b2e3rnvLm', 'ADMIN', TRUE),
    ('alex', '$2a$10$5HORknBKFVdRcnjEBMzBUemN/0MTLcnA53jAzZxEiQLkQfK5gVI3q', 'USER', TRUE),
    ('maria', '$2a$10$DJ7vqjwORKgmk/XqiFir2uMJLeH5OSMc/fVAgbkgGQjGWv3SpnBme', 'USER', TRUE);
INSERT INTO coins (id, country, denomination, year) VALUES
    (1, 'Bulgaria', '1 Lev', 2002),
    (2, 'Germany', '1 Euro', 2010),
    (3, 'France', '2 Euro', 2015),
    (4, 'Italy', '50 Centesimi', 2007),
    (5, 'Spain', '20 Centesimos', 2003),
    (6, 'Greece', '1 Euro', 2008);

-- A quantity of 0 (e.g. vince/coin 3 and 4, maria/coin 1) is intentional: it exercises the
-- "explicit zero counts as not-owned" behavior in the owned/missing coin queries (see
-- PostgresCollectionEntryRepository) rather than every seeded coin being either owned or
-- entirely absent.
INSERT INTO collection_entries (user_id, coin_id, quantity) VALUES
    (1, 1, 2),
    (1, 2, 1),
    (1, 3, 0),
    (1, 4, 0),
    (2, 2, 3),
    (2, 5, 1),
    (3, 1, 0),
    (3, 6, 2);