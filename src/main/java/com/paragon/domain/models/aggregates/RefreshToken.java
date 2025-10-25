package com.paragon.domain.models.aggregates;

import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.aggregate.RefreshTokenException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenExceptionInfo;
import com.paragon.domain.interfaces.TokenHasher;
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
    private final Instant expiresAt;
    private boolean isRevoked;
    private final RefreshTokenId replacedBy;
    private static final Duration EXPIRY_DURATION = Duration.ofDays(7);

    private RefreshToken(RefreshTokenId refreshTokenId,
                         RefreshTokenHash tokenHash,
                         StaffAccountId staffAccountId,
                         Instant expiresAt,
                         boolean isRevoked,
                         RefreshTokenId replacedBy,
                         Version version
    ) {
        super(refreshTokenId);
        this.tokenHash = tokenHash;
        this.staffAccountId = staffAccountId;
        this.expiresAt = expiresAt;
        this.isRevoked = isRevoked;
        this.replacedBy = replacedBy;
        this.version = version;
    }

    public static RefreshToken issue(StaffAccountId staffAccountId, TokenHasher tokenHasher) {
        if (staffAccountId == null) {
            throw new RefreshTokenException(RefreshTokenExceptionInfo.staffAccountIdRequired());
        }

        RefreshTokenHash refreshTokenHash = RefreshTokenHash.generate(tokenHasher);
        return new RefreshToken(
                RefreshTokenId.generate(), refreshTokenHash, staffAccountId, Instant.now().plus(EXPIRY_DURATION),
                false, null, Version.initial()
        );
    }
}
