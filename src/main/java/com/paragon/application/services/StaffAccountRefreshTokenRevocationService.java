package com.paragon.application.services;

import com.paragon.domain.models.valueobjects.StaffAccountId;

public interface StaffAccountRefreshTokenRevocationService {
    void revokeAllTokensForStaffAccount(StaffAccountId staffAccountId);
}
