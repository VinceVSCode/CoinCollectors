DELETE FROM collection_entries;
DELETE FROM users;
DELETE FROM coins;

ALTER SEQUENCE IF EXISTS collection_entries_id_seq RESTART WITH 1;