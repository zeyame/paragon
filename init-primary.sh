#!/bin/bash
set -e

echo "Adding replication config..."
echo "host replication all 0.0.0.0/0 trust" >> "$PGDATA/pg_hba.conf"

echo "Initializing primary database..."
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE paragon_write;
    CREATE USER paragon_user WITH ENCRYPTED PASSWORD 'paragon_password' REPLICATION;
    GRANT ALL PRIVILEGES ON DATABASE paragon_write TO paragon_user;
EOSQL

echo "✅ Primary database and replication user configured."
