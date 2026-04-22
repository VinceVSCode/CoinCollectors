CREATE TABLE collection_entries (
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