package com.paragon.helpers.fixtures;

import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import com.paragon.domain.models.valueobjects.RefreshTokenId;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Version;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class RefreshTokenFixture {
    private String id = UUID.randomUUID().toString();
    private String tokenHash = "hashed_token_" + UUID.randomUUID();
    private String staffAccountId = UUID.randomUUID().toString();
    private Instant expiresAt = Instant.now().plus(Duration.ofDays(7));
    private boolean isRevoked = false;
    private String replacedBy = null;
    private int version = 1;

    public RefreshTokenFixture withId(String value) {
        this.id = value;
        return this;
    }

    public RefreshTokenFixture withTokenHash(String value) {
        this.tokenHash = value;
        return this;
    }

    public RefreshTokenFixture withStaffAccountId(String value) {
        this.staffAccountId = value;
        return this;
    }

    public RefreshTokenFixture withExpiresAt(Instant value) {
        this.expiresAt = value;
        return this;
    }

    public RefreshTokenFixture withRevoked(boolean value) {
        this.isRevoked = value;
        return this;
    }

    public RefreshTokenFixture withReplacedBy(String value) {
        this.replacedBy = value;
        return this;
    }

    public RefreshTokenFixture withVersion(int value) {
        this.version = value;
        return this;
    }

    public RefreshToken build() {
        return RefreshToken.createFrom(
                RefreshTokenId.from(id),
                RefreshTokenHash.fromHashed(tokenHash),
                StaffAccountId.from(staffAccountId),
                expiresAt,
                isRevoked,
                replacedBy != null ? RefreshTokenId.from(replacedBy) : null,
                Version.of(version)
        );
    }

    public static RefreshToken validRefreshToken() {
        return new RefreshTokenFixture().build();
    }
}