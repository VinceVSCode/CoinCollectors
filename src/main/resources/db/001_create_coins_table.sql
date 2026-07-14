-- Legacy manual-execution script (see db/README.md), superseded by the Flyway migrations in
-- db/migration/ that DatabaseBootstrap actually runs at startup. Not referenced by any Java
-- code; kept for historical/manual-ops reference only.
CREATE TABLE coins(
  id INTEGER PRIMARY KEY ,
  country TEXT NOT NULL,
  denomination TEXT NOT NULL,
  year INTEGER NOT NULL CHECK (year>0)
);