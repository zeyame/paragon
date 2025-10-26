package com.paragon.domain.interfaces.services;

import com.paragon.domain.models.valueobjects.StaffAccountId;

public interface RefreshTokenRevocationService {
    void revokeAllTokensForStaffAccount(StaffAccountId staffAccountId);
}
