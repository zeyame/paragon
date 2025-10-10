CREATE TABLE IF NOT EXISTS staff_account_permissions (
    staff_account_id     UUID NOT NULL,
    permission_code      VARCHAR(64) NOT NULL,
    assigned_by          UUID NOT NULL,
    assigned_at_utc      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    PRIMARY KEY (staff_account_id, permission_code),

    CONSTRAINT fk_staff_account_permissions_staff_account
        FOREIGN KEY (staff_account_id)
        REFERENCES staff_accounts (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_staff_account_permissions_permission_code
        FOREIGN KEY (permission_code)
        REFERENCES permissions (code)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_staff_account_permissions_assigned_by
        FOREIGN KEY (assigned_by)
        REFERENCES staff_accounts (id)
        ON DELETE SET NULL
);
