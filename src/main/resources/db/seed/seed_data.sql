INSERT INTO users (id, username) VALUES
     (1, 'vince'),
     (2, 'alex'),
     (3, 'maria');

INSERT INTO coins (id, country, denomination, year) VALUES
    (1, 'Bulgaria', '1 Lev', 2002),
    (2, 'Germany', '1 Euro', 2010),
    (3, 'France', '2 Euro', 2015),
    (4, 'Italy', '50 Centesimi', 2007),
    (5, 'Spain', '20 Centesimos', 2003),
    (6, 'Greece', '1 Euro', 2008);

INSERT INTO collection_entries (id, user_id, coin_id, quantity) VALUES
    (1, 1, 1, 2),
    (2, 1, 2, 1),
    (3, 1, 3, 0),
    (4, 1, 4, 0),
    (5, 2, 2, 3),
    (6, 2, 5, 1),
    (7, 3, 1, 0),
    (8, 3, 6, 2);