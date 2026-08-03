-- Same retrofit as V2 (collection_entries) and V4 (users), now for `coins.id` — required so
-- the admin catalog endpoints (PostgresCoinCatalogCommandRepository#createCoin) can add coins
-- without the caller inventing an id. Until now the only rows came from seed_data.sql, which
-- supplies ids explicitly.
CREATE SEQUENCE IF NOT EXISTS coins_id_seq
    START WITH 1
    INCREMENT BY 1;

-- Seeds the sequence past whatever ids already exist so the first generated id can't collide
-- with a seeded coin (seed_data.sql hardcodes 1-6).
SELECT setval(
               'coins_id_seq',
               COALESCE((SELECT MAX(id) FROM coins), 1),
               false
       );

ALTER TABLE coins
    ALTER COLUMN id SET DEFAULT nextval('coins_id_seq');
