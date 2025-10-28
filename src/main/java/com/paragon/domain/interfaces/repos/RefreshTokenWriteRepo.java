package com.paragon.domain.interfaces.repos;

import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;

import java.util.List;

public interface RefreshTokenWriteRepo {
    void create(RefreshToken refreshToken);
    List<RefreshToken> getActiveTokensByStaffAccountId(StaffAccountId staffAccountId);
    int updateAll(List<RefreshToken> refreshTokens);
}
