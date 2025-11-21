package com.paragon.domain.interfaces;

import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import com.paragon.domain.models.valueobjects.StaffAccountId;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenWriteRepo {
    void create(RefreshToken refreshToken);
    Optional<RefreshToken> getByTokenHash(RefreshTokenHash tokenHash);
    List<RefreshToken> getActiveTokensByStaffAccountId(StaffAccountId staffAccountId);
    void update(RefreshToken refreshToken);
    void updateAll(List<RefreshToken> refreshTokens);
}
