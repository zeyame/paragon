package com.paragon.domain.services;

import com.paragon.domain.interfaces.services.RefreshTokenRevocationService;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenRevocationServiceImpl implements RefreshTokenRevocationService {
    @Override
    public void revokeAllTokensForStaffAccount(StaffAccountId staffAccountId) {}
}
