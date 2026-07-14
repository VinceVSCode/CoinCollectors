-- V1 left `id` as a plain INTEGER PRIMARY KEY with no default, requiring every INSERT to
-- supply an id explicitly. This migration retrofits a sequence-backed default so
-- PostgresCollectionEntryRepository#addCollectionEntry can insert without one and read the
-- generated id back via GeneratedKeyHolder — matching how V4 does the same for `users`.
CREATE SEQUENCE IF NOT EXISTS collection_entries_id_seq
    START WITH 1
    INCREMENT BY 1;

-- Seeds the sequence's next value from whatever ids already exist, so pre-existing rows
-- (inserted the old explicit-id way, e.g. by seed_data.sql before this migration) don't
-- collide with the first auto-generated id.
SELECT setval(
               'collection_entries_id_seq',
               COALESCE((SELECT MAX(id) FROM collection_entries), 1),
               false
       );

ALTER TABLE collection_entries
    ALTER COLUMN id SET DEFAULT nextval('collection_entries_id_seq');