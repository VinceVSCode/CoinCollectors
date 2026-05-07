CREATE SEQUENCE IF NOT EXISTS collection_entries_id_seq
    START WITH 1
    INCREMENT BY 1;

SELECT setval(
               'collection_entries_id_seq',
               COALESCE((SELECT MAX(id) FROM collection_entries), 1),
               false
       );

ALTER TABLE collection_entries
    ALTER COLUMN id SET DEFAULT nextval('collection_entries_id_seq');