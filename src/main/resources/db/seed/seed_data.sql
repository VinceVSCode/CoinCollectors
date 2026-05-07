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

INSERT INTO collection_entries (user_id, coin_id, quantity) VALUES
    (1, 1, 2),
    (1, 2, 1),
    (1, 3, 0),
    (1, 4, 0),
    (2, 2, 3),
    (2, 5, 1),
    (3, 1, 0),
    (3, 6, 2);