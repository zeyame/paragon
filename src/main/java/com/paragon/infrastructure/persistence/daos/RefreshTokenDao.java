package com.paragon.infrastructure.persistence.daos;

import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.*;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenDao(
        UUID id,
        UUID staffAccountId,
        String tokenHash,
        String issuedFromIpAddress,
        Instant expiresAtUtc,
        boolean isRevoked,
        Instant revokedAtUtc,
        UUID replacedBy,
        int version,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {

    public RefreshToken toRefreshToken() {
        return RefreshToken.createFrom(
                RefreshTokenId.of(id),
                RefreshTokenHash.fromHashed(tokenHash),
                StaffAccountId.of(staffAccountId),
                IpAddress.of(issuedFromIpAddress),
                expiresAtUtc,
                isRevoked,
                revokedAtUtc,
                replacedBy != null ? RefreshTokenId.of(replacedBy) : null,
                Version.of(version)
        );
    }
}