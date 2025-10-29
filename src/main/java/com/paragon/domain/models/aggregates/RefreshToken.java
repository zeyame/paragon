package com.paragon.domain.models.aggregates;

import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.aggregate.RefreshTokenException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenExceptionInfo;
import com.paragon.domain.interfaces.TokenHasher;
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
    private final RefreshTokenId replacedBy;
    private static final Duration EXPIRY_DURATION = Duration.ofDays(7);

    private RefreshToken(RefreshTokenId refreshTokenId,
                         RefreshTokenHash tokenHash,
                         StaffAccountId staffAccountId,
                         IpAddress issuedFromIpAddress,
                         Instant expiresAt,
                         boolean isRevoked,
                         RefreshTokenId replacedBy,
                         Version version
    ) {
        super(refreshTokenId);
        this.tokenHash = tokenHash;
        this.staffAccountId = staffAccountId;
        this.issuedFromIpAddress = issuedFromIpAddress;
        this.expiresAt = expiresAt;
        this.isRevoked = isRevoked;
        this.replacedBy = replacedBy;
        this.version = version;
    }

    public static RefreshToken issue(StaffAccountId staffAccountId, IpAddress ipAddress, TokenHasher tokenHasher) {
        assertValidTokenIssuance(staffAccountId, ipAddress);

        RefreshTokenHash refreshTokenHash = RefreshTokenHash.generate(tokenHasher);
        return new RefreshToken(
                RefreshTokenId.generate(), refreshTokenHash, staffAccountId, ipAddress,
                Instant.now().plus(EXPIRY_DURATION), false, null, Version.initial()
        );
    }

    private static void assertValidTokenIssuance(StaffAccountId staffAccountId, IpAddress ipAddress) {
        if (staffAccountId == null) {
            throw new RefreshTokenException(RefreshTokenExceptionInfo.staffAccountIdRequired());
        }
        if (ipAddress == null) {
            throw new RefreshTokenException(RefreshTokenExceptionInfo.ipAddressRequired());
        }
    }

    public void revoke() {
        if (isRevoked) {
            throw new RefreshTokenException(RefreshTokenExceptionInfo.tokenAlreadyRevoked());
        }
        this.isRevoked = true;
    }

    public static RefreshToken createFrom(RefreshTokenId refreshTokenId,
                                          RefreshTokenHash tokenHash,
                                          StaffAccountId staffAccountId,
                                          IpAddress issuedFromIpAddress,
                                          Instant expiresAt,
                                          boolean isRevoked,
                                          RefreshTokenId replacedBy,
                                          Version version) {
        return new RefreshToken(
                refreshTokenId,
                tokenHash,
                staffAccountId,
                issuedFromIpAddress,
                expiresAt,
                isRevoked,
                replacedBy,
                version
        );
    }
}
