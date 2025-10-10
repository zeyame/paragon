CREATE EXTENSION IF NOT EXISTS "pgcrypto";

INSERT INTO permissions (id, code, category, description, created_at_utc, updated_at_utc) VALUES
    -- ACCOUNTS
    (gen_random_uuid(), 'VIEW_ACCOUNT_LIST', 'ACCOUNTS', 'Can view the list of staff accounts', now(), now()),
    (gen_random_uuid(), 'MANAGE_ACCOUNTS', 'ACCOUNTS', 'Can create, update, and delete staff accounts', now(), now()),
    (gen_random_uuid(), 'RESET_ACCOUNT_PASSWORD', 'ACCOUNTS', 'Can reset passwords for staff accounts', now(), now()),
    (gen_random_uuid(), 'APPROVE_PASSWORD_CHANGE', 'ACCOUNTS', 'Can approve staff password change requests', now(), now()),

    -- ORDERS
    (gen_random_uuid(), 'APPROVE_ORDER_ACCESS', 'ORDERS', 'Can approve access to restricted order data', now(), now()),
    (gen_random_uuid(), 'VIEW_ORDER_LIST', 'ORDERS', 'Can view list of customer orders', now(), now()),
    (gen_random_uuid(), 'REQUEST_CENSORED_ORDER_CONTENT', 'ORDERS', 'Can request access to censored order content', now(), now()),
    (gen_random_uuid(), 'INSTANT_ACCESS_CENSORED_ORDER_CONTENT', 'ORDERS', 'Can instantly access censored order content', now(), now()),
    (gen_random_uuid(), 'CREATE_ORDER_PUBLIC_SHARE_LINK', 'ORDERS', 'Can generate public share links for orders', now(), now()),
    (gen_random_uuid(), 'VIEW_EARLY_DISPUTE_WARNINGS', 'ORDERS', 'Can view early dispute detection warnings', now(), now()),

    -- PRODUCTS
    (gen_random_uuid(), 'VIEW_PRODUCT_LIST', 'PRODUCTS', 'Can view product list', now(), now()),
    (gen_random_uuid(), 'VIEW_PRODUCT_STOCK_COUNT', 'PRODUCTS', 'Can view product stock counts', now(), now()),
    (gen_random_uuid(), 'VIEW_PRODUCT_SALES_COUNT', 'PRODUCTS', 'Can view product sales statistics', now(), now()),

    -- LOGGING
    (gen_random_uuid(), 'VIEW_LOGIN_LOGS', 'LOGGING', 'Can view login activity logs', now(), now()),
    (gen_random_uuid(), 'VIEW_PASSWORD_CHANGE_LOGS', 'LOGGING', 'Can view password change logs', now(), now()),
    (gen_random_uuid(), 'VIEW_MODMAIL_LOGS', 'LOGGING', 'Can view modmail logs', now(), now()),
    (gen_random_uuid(), 'VIEW_WOOCOMMERCE_LOGS', 'LOGGING', 'Can view WooCommerce integration logs', now(), now()),

    -- DISCORD_BACKUP
    (gen_random_uuid(), 'VIEW_BACKUP_LIST', 'DISCORD_BACKUP', 'Can view list of Discord backups', now(), now()),
    (gen_random_uuid(), 'ACCESS_BACKUP_CONTENT', 'DISCORD_BACKUP', 'Can access Discord backup contents', now(), now()),
    (gen_random_uuid(), 'LOAD_BACKUP_CONTENT', 'DISCORD_BACKUP', 'Can load or restore Discord backups', now(), now()),

    -- DISCORD_MEMBERS
    (gen_random_uuid(), 'VIEW_SYNCED_MEMBERS', 'DISCORD_MEMBERS', 'Can view synced Discord members', now(), now()),
    (gen_random_uuid(), 'MIGRATE_SYNCED_MEMBERS', 'DISCORD_MEMBERS', 'Can migrate synced Discord members', now(), now()),

    -- TRANSCRIPTS
    (gen_random_uuid(), 'APPROVE_MODMAIL_TRANSCRIPT_ACCESS', 'TRANSCRIPTS', 'Can approve access to modmail transcripts', now(), now()),
    (gen_random_uuid(), 'VIEW_MODMAIL_TRANSCRIPT_LIST', 'TRANSCRIPTS', 'Can view list of modmail transcripts', now(), now()),
    (gen_random_uuid(), 'CREATE_MODMAIL_TRANSCRIPT_PUBLIC_SHARE_LINK', 'TRANSCRIPTS', 'Can create public share links for modmail transcripts', now(), now());
