-- Audit trail for privileged mutations (role/active changes, catalog create/update/delete).
CREATE SEQUENCE IF NOT EXISTS admin_actions_id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS admin_actions (
    id INTEGER PRIMARY KEY DEFAULT nextval('admin_actions_id_seq'),
    -- Deliberately NOT a foreign key to users, and the username is denormalized alongside the
    -- id: an audit record has to outlive the account that produced it, so neither a future
    -- account deletion nor a username change may rewrite or cascade away history.
    actor_user_id INTEGER NOT NULL,
    actor_username TEXT NOT NULL,
    action TEXT NOT NULL,
    -- Same reasoning for the target: no FK, because the most interesting thing to audit is a
    -- deletion, and an FK would delete the evidence along with the row.
    target_type TEXT NOT NULL,
    target_id INTEGER,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- The only read pattern is "most recent first", which this index serves directly.
CREATE INDEX IF NOT EXISTS idx_admin_actions_created_at
    ON admin_actions (created_at DESC, id DESC);
