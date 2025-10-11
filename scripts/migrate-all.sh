#!/bin/bash
set -e

echo "=== Migrating db_write ==="
flyway -url=jdbc:postgresql://db_write:5432/paragon_write \
       -user=paragon_user \
       -password=paragon_password \
       -locations=filesystem:/flyway/sql \
       -validateMigrationNaming=true \
       migrate

echo "=== Migrating db_read ==="
flyway -url=jdbc:postgresql://db_read:5432/paragon_write \
       -user=paragon_user \
       -password=paragon_password \
       -locations=filesystem:/flyway/sql \
       -validateMigrationNaming=true \
       migrate

echo "=== Migrating paragon_db_test ==="
flyway -url=jdbc:postgresql://paragon_db_test:5432/paragon_test \
       -user=test_user \
       -password=test_password \
       -locations=filesystem:/flyway/sql \
       -validateMigrationNaming=true \
       migrate
