CREATE TABLE IF NOT EXISTS staff_account_requests (
    id                          UUID PRIMARY KEY,

    submitted_by                UUID NOT NULL,
    request_type                VARCHAR(64) NOT NULL,
    target_id                   TEXT,
    target_type                 VARCHAR(64),
    status                      VARCHAR(32) NOT NULL,
    submitted_at_utc            TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at_utc              TIMESTAMP WITH TIME ZONE NOT NULL,
    approved_by                 UUID,
    approved_at_utc             TIMESTAMP WITH TIME ZONE,
    rejected_by                 UUID,
    rejected_at_utc             TIMESTAMP WITH TIME ZONE,

    version                     INTEGER NOT NULL DEFAULT 1,

    updated_at_utc              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_staff_account_requests_submitted_by
        FOREIGN KEY (submitted_by)
        REFERENCES staff_accounts (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_staff_account_requests_approved_by
        FOREIGN KEY (approved_by)
        REFERENCES staff_accounts (id)
        ON DELETE SET NULL,

    CONSTRAINT fk_staff_account_requests_rejected_by
        FOREIGN KEY (rejected_by)
        REFERENCES staff_accounts (id)
        ON DELETE SET NULL,

    CONSTRAINT chk_target_id_and_type_both_or_neither
        CHECK (
            (target_id IS NULL AND target_type IS NULL) OR
            (target_id IS NOT NULL AND target_type IS NOT NULL)
        ),

    CONSTRAINT chk_only_one_decision
        CHECK (
            (approved_by IS NULL AND approved_at_utc IS NULL) OR
            (rejected_by IS NULL AND rejected_at_utc IS NULL)
        ),

    CONSTRAINT chk_approved_fields_together
        CHECK (
            (approved_by IS NULL AND approved_at_utc IS NULL) OR
            (approved_by IS NOT NULL AND approved_at_utc IS NOT NULL)
        ),

    CONSTRAINT chk_rejected_fields_together
        CHECK (
            (rejected_by IS NULL AND rejected_at_utc IS NULL) OR
            (rejected_by IS NOT NULL AND rejected_at_utc IS NOT NULL)
        )
);

CREATE INDEX ix_staff_account_requests_submitted_by ON staff_account_requests (submitted_by);
CREATE INDEX ix_staff_account_requests_status ON staff_account_requests (status);
CREATE INDEX ix_staff_account_requests_request_type ON staff_account_requests (request_type);
CREATE INDEX ix_staff_account_requests_expires_at_utc ON staff_account_requests (expires_at_utc);
CREATE INDEX ix_staff_account_requests_target_type ON staff_account_requests (target_type);
CREATE INDEX ix_staff_account_requests_approved_by ON staff_account_requests (approved_by);
CREATE INDEX ix_staff_account_requests_rejected_by ON staff_account_requests (rejected_by);