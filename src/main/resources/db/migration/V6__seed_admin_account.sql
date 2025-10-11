INSERT INTO staff_accounts (
    id,
    username,
    email,
    password,
    password_issued_at_utc,
    order_access_duration,
    modmail_transcript_access_duration,
    status,
    failed_login_attempts,
    locked_until_utc,
    last_login_at_utc,
    created_by,
    disabled_by,
    version,
    created_at_utc,
    updated_at_utc
)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin',
    NULL,
    '$2a$10$8lX4Kx1IehayULqLK/XOBu8D2L/RpY1Zk8x1.2MIJ1rPwZo6kAohK',
    NOW(),
    999999,
    999999,
    'ACTIVE',
    0,
    NULL,
    NULL,
    '00000000-0000-0000-0000-000000000001',
    NULL,
    1,
    NOW(),
    NOW()
);

INSERT INTO staff_account_permissions (
    staff_account_id,
    permission_code,
    assigned_by,
    assigned_at_utc
)
SELECT
    '00000000-0000-0000-0000-000000000001',
    p.code,
    '00000000-0000-0000-0000-000000000001',
    NOW()
FROM permissions p;