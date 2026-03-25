CREATE TABLE coins(
  id INTEGER PRIMARY KEY ,
  country TEXT NOT NULL,
  denomination TEXT NOT NULL,
  year INTEGER NOT NULL CHECK (year>0)
);