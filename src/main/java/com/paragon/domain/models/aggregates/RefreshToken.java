package com.paragon.domain.models.aggregates;

import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.aggregate.RefreshTokenException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenExceptionInfo;
import com.paragon.domain.models.valueobjects.IpAddress;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import com.paragon.domain.models.valueobjects.RefreshTokenId;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Version;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

@Getter
public class RefreshToken extends EventSourcedAggregate<DomainEvent, RefreshTokenId> {
    private final RefreshTokenHash tokenHash;
    private final StaffAccountId staffAccountId;
    private final IpAddress issuedFromIpAddress;
    private final Instant expiresAt;
    private boolean isRevoked;
    private Instant revokedAt;
    private RefreshTokenId replacedBy;
    private static final Duration EXPIRY_DURATION = Duration.ofDays(7);

    private RefreshToken(RefreshTokenId refreshTokenId,
                         RefreshTokenHash tokenHash,
                         StaffAccountId staffAccountId,
                         IpAddress issuedFromIpAddress,
                         Instant expiresAt,
                         boolean isRevoked,
                         Instant revokedAt,
                         RefreshTokenId replacedBy,
                         Version version
    ) {
        super(refreshTokenId);
        this.tokenHash = tokenHash;
        this.staffAccountId = staffAccountId;
        this.issuedFromIpAddress = issuedFromIpAddress;
        this.expiresAt = expiresAt;
        this.isRevoked = isRevoked;
        this.revokedAt = revokedAt;
        this.replacedBy = replacedBy;
        this.version = version;
    }

    public static RefreshToken issue(RefreshTokenHash refreshTokenHash, StaffAccountId staffAccountId, IpAddress ipAddress) {
        assertValidTokenIssuance(refreshTokenHash, staffAccountId, ipAddress);
        return new RefreshToken(
                RefreshTokenId.generate(), refreshTokenHash, staffAccountId, ipAddress,
                Instant.now().plus(EXPIRY_DURATION), false, null, null, Version.initial()
        );
    }

    public void revoke() {
        throwIfTokenIsAlreadyRevoked();
        this.isRevoked = true;
        this.revokedAt = Instant.now();
        increaseVersion();
    }

    public RefreshToken replace(RefreshTokenHash newTokenHash) {
        throwIfTokenIsAlreadyRevoked();
        this.isRevoked = true;
        this.revokedAt = Instant.now();

        RefreshTokenId newTokenId = RefreshTokenId.generate();
        this.replacedBy = newTokenId;
        increaseVersion();

        return new RefreshToken(
                newTokenId, newTokenHash, staffAccountId, issuedFromIpAddress,
                Instant.now().plus(EXPIRY_DURATION), false, null, null, Version.initial()
        );
    }

    public boolean isExpired() {
        return !expiresAt.isAfter(Instant.now());
    }

    public static RefreshToken createFrom(RefreshTokenId refreshTokenId,
                                          RefreshTokenHash tokenHash,
                                          StaffAccountId staffAccountId,
                                          IpAddress issuedFromIpAddress,
                                          Instant expiresAt,
                                          boolean isRevoked,
                                          Instant revokedAt,
                                          RefreshTokenId replacedBy,
                                          Version version) {
        return new RefreshToken(
                refreshTokenId,
                tokenHash,
                staffAccountId,
                issuedFromIpAddress,
                expiresAt,
                isRevoked,
                revokedAt,
                replacedBy,
                version
        );
    }

    private static void assertValidTokenIssuance(RefreshTokenHash refreshTokenHash, StaffAccountId staffAccountId, IpAddress ipAddress) {
        if (refreshTokenHash == null) {
            throw new RefreshTokenException(RefreshTokenExceptionInfo.tokenHashRequired());
        }
        if (staffAccountId == null) {
            throw new RefreshTokenException(RefreshTokenExceptionInfo.staffAccountIdRequired());
        }
        if (ipAddress == null) {
            throw new RefreshTokenException(RefreshTokenExceptionInfo.ipAddressRequired());
        }
    }

    private void throwIfTokenIsAlreadyRevoked() {
        if (isRevoked) {
            throw new RefreshTokenException(RefreshTokenExceptionInfo.tokenAlreadyRevoked());
        }
    }
}
