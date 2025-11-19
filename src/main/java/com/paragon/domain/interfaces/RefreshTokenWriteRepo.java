package com.paragon.domain.interfaces;

import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;

import java.util.List;

public interface RefreshTokenWriteRepo {
    void create(RefreshToken refreshToken);
    List<RefreshToken> getActiveTokensByStaffAccountId(StaffAccountId staffAccountId);
    void updateAll(List<RefreshToken> refreshTokens);
}
