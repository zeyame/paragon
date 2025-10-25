CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                  UUID PRIMARY KEY,

    staff_account_id    UUID NOT NULL REFERENCES staff_accounts(id),
    token_hash          VARCHAR(64) NOT NULL,
    expires_at_utc      TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked          BOOLEAN NOT NULL DEFAULT false,
    revoked_at_utc      TIMESTAMP WITH TIME ZONE,
    replaced_by         UUID REFERENCES refresh_tokens(id),

    version             INTEGER NOT NULL DEFAULT 1,

    created_at_utc      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at_utc      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX ix_refresh_tokens_staff_account_id ON refresh_tokens (staff_account_id);
CREATE INDEX ix_refresh_tokens_expires_at_utc ON refresh_tokens (expires_at_utc);