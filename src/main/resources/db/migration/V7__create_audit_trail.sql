CREATE TABLE IF NOT EXISTS audit_trail (
    id              UUID PRIMARY KEY,
    created_at_utc  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    actor_id        UUID NOT NULL,
    action_type     VARCHAR(64) NOT NULL,
    target_id       TEXT,
    target_type     VARCHAR(64),
    outcome         VARCHAR(32) NOT NULL,
    ip_address      VARCHAR(64),
    correlation_id  VARCHAR(128),

    CONSTRAINT fk_audit_trail_actor
        FOREIGN KEY (actor_id)
        REFERENCES staff_accounts (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_audit_trail_actor_id ON audit_trail (actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_trail_action_type ON audit_trail (action_type);
CREATE INDEX IF NOT EXISTS idx_audit_trail_target_type ON audit_trail (target_type);
CREATE INDEX IF NOT EXISTS idx_audit_trail_correlation_id ON audit_trail (correlation_id);
