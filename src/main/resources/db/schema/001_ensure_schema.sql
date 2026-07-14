-- Legacy manual-execution script combining db/001-003 into one idempotent (IF NOT EXISTS)
-- file — same "superseded by Flyway, not run by any code" status as those three. See
-- db/README.md for the original recommended manual execution order (schema -> reset -> seed).
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
