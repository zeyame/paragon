package com.paragon.helpers.fixtures;

import com.paragon.infrastructure.persistence.daos.RefreshTokenDao;

import java.time.Instant;
import java.util.UUID;

public class RefreshTokenDaoFixture {
    private UUID id = UUID.randomUUID();
    private UUID staffAccountId = UUID.randomUUID();
    private String tokenHash = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2";
    private String issuedFromIpAddress = "192.168.1.1";
    private Instant expiresAtUtc = Instant.now().plusSeconds(604800); // 7 days
    private boolean isRevoked = false;
    private Instant revokedAtUtc = null;
    private UUID replacedBy = null;
    private int version = 1;
    private Instant createdAtUtc = Instant.now();
    private Instant updatedAtUtc = Instant.now();

    public RefreshTokenDaoFixture withId(UUID value) {
        this.id = value;
        return this;
    }

    public RefreshTokenDaoFixture withStaffAccountId(UUID value) {
        this.staffAccountId = value;
        return this;
    }

    public RefreshTokenDaoFixture withTokenHash(String value) {
        this.tokenHash = value;
        return this;
    }

    public RefreshTokenDaoFixture withIssuedFromIpAddress(String value) {
        this.issuedFromIpAddress = value;
        return this;
    }

    public RefreshTokenDaoFixture withExpiresAtUtc(Instant value) {
        this.expiresAtUtc = value;
        return this;
    }

    public RefreshTokenDaoFixture withIsRevoked(boolean value) {
        this.isRevoked = value;
        return this;
    }

    public RefreshTokenDaoFixture withRevokedAtUtc(Instant value) {
        this.revokedAtUtc = value;
        return this;
    }

    public RefreshTokenDaoFixture withReplacedBy(UUID value) {
        this.replacedBy = value;
        return this;
    }

    public RefreshTokenDaoFixture withVersion(int value) {
        this.version = value;
        return this;
    }

    public RefreshTokenDaoFixture withCreatedAtUtc(Instant value) {
        this.createdAtUtc = value;
        return this;
    }

    public RefreshTokenDaoFixture withUpdatedAtUtc(Instant value) {
        this.updatedAtUtc = value;
        return this;
    }

    public RefreshTokenDao build() {
        return new RefreshTokenDao(
                id,
                staffAccountId,
                tokenHash,
                issuedFromIpAddress,
                expiresAtUtc,
                isRevoked,
                revokedAtUtc,
                replacedBy,
                version,
                createdAtUtc,
                updatedAtUtc
        );
    }

    public static RefreshTokenDao validRefreshTokenDao() {
        return new RefreshTokenDaoFixture().build();
    }

    public static RefreshTokenDao revokedRefreshTokenDao() {
        return new RefreshTokenDaoFixture()
                .withIsRevoked(true)
                .withRevokedAtUtc(Instant.now())
                .build();
    }

    public static RefreshTokenDao expiredRefreshTokenDao() {
        return new RefreshTokenDaoFixture()
                .withExpiresAtUtc(Instant.now().minusSeconds(3600)) // expired 1 hour ago
                .build();
    }
}