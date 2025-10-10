CREATE TABLE IF NOT EXISTS permissions (
    id              UUID PRIMARY KEY,
    code            VARCHAR(64) NOT NULL UNIQUE,
    category        VARCHAR(64) NOT NULL,
    description     TEXT,
    created_at_utc  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at_utc  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
