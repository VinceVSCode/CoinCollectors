-- Flyway migration, run automatically by DatabaseBootstrap.initialize() before the Spring
-- context starts. Establishes the three core tables + FKs; V2-V4 layer auth and generated-id
-- changes on top of this without altering these CREATE TABLE statements.
CREATE TABLE IF NOT EXISTS coins (
    id INTEGER PRIMARY KEY,
    country TEXT NOT NULL,
    denomination TEXT NOT NULL,
    year INTEGER NOT NULL CHECK (year > 0)
);

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE
);

-- The (user_id, coin_id) pair is the real identity of a collection entry; the surrogate `id`
-- exists mainly so CollectionEntryRepositoryInterface can address rows by a single key.
-- ON DELETE CASCADE means deleting a user or coin silently drops their collection entries too.
CREATE TABLE IF NOT EXISTS collection_entries (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    coin_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    CONSTRAINT fk_collection_entries_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_collection_entries_coin
        FOREIGN KEY (coin_id) REFERENCES coins(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_collection_entries_user_coin
        UNIQUE (user_id, coin_id)
);