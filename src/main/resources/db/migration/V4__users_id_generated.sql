-- Same retrofit as V2, but for `users.id` — required so UserRegistrationService can register
-- new accounts (via PostgresAuthUserRepository#createAuthUser) without callers ever having to
-- pick an id themselves. seed_data.sql's comment documents the resulting vince=1/alex=2/maria=3
-- ordering this sequence produces for the dev seed data.
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
