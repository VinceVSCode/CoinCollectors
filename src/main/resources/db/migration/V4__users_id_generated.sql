-- Same retrofit as V2, but for `users.id` — required so UserRegistrationService can register
-- new accounts (via PostgresAuthUserRepository#createAuthUser) without callers ever having to
-- pick an id themselves. Note this sequence is never restarted once created, so the ids it
-- hands out to the dev seed accounts are only 1/2/3 on a pristine database — seed_data.sql
-- must not (and does not) assume any particular value.
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
