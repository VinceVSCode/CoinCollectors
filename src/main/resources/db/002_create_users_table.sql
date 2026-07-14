-- Legacy manual-execution script (see db/README.md) — superseded by db/migration/V1, which
-- DatabaseBootstrap actually runs. Not wired into the app; kept for reference only.
CREATE TABLE users (
   id INTEGER PRIMARY KEY,
   username TEXT NOT NULL UNIQUE
);