CREATE SEQUENCE IF NOT EXISTS users_id_seq
    START WITH 1
    INCREMENT BY 1;

SELECT setval(
               'users_id_seq',
               COALESCE((SELECT MAX(id) FROM users), 1),
               false
       );

ALTER TABLE users
    ALTER COLUMN id SET DEFAULT nextval('users_id_seq');
