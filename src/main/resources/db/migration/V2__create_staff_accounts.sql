CREATE TABLE IF NOT EXISTS staff_accounts (
    id                              UUID PRIMARY KEY,

    username                        VARCHAR(64) NOT NULL,
    email                           VARCHAR(255) NULL,
    password                        TEXT NOT NULL,
    is_password_temporary           BOOLEAN NOT NULL DEFAULT true,
    password_issued_at_utc           TIMESTAMP WITH TIME ZONE,
    order_access_duration            INTEGER NOT NULL,
    modmail_transcript_access_duration INTEGER NOT NULL,
    status                          VARCHAR(32) NOT NULL,
    failed_login_attempts           INTEGER NOT NULL DEFAULT 0,
    locked_until_utc                TIMESTAMP WITH TIME ZONE,
    last_login_at_utc               TIMESTAMP WITH TIME ZONE,

    created_by                      UUID NOT NULL REFERENCES staff_accounts(id),
    disabled_by                     UUID REFERENCES staff_accounts(id),
    password_reset_by               UUID REFERENCES staff_accounts(id),

    version                         INTEGER NOT NULL DEFAULT 1,

    created_at_utc                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at_utc                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_staff_accounts_username ON staff_accounts (LOWER(username));
CREATE UNIQUE INDEX ux_staff_accounts_email    ON staff_accounts (LOWER(email));
