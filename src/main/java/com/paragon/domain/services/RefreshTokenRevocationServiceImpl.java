package com.paragon.domain.services;

import com.paragon.domain.exceptions.services.RefreshTokenRevocationServiceException;
import com.paragon.domain.exceptions.services.RefreshTokenRevocationServiceExceptionInfo;
import com.paragon.domain.interfaces.repos.RefreshTokenWriteRepo;
import com.paragon.domain.interfaces.services.RefreshTokenRevocationService;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RefreshTokenRevocationServiceImpl implements RefreshTokenRevocationService {
    private final RefreshTokenWriteRepo refreshTokenWriteRepo;
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenRevocationServiceImpl.class);

    public RefreshTokenRevocationServiceImpl(RefreshTokenWriteRepo refreshTokenWriteRepo) {
        this.refreshTokenWriteRepo = refreshTokenWriteRepo;
    }

    @Override
    public void revokeAllTokensForStaffAccount(StaffAccountId staffAccountId) {
        log.info("Attempting to revoke all active refresh tokens for staff account with ID: {}", staffAccountId.getValue());

        List<RefreshToken> activeTokens = refreshTokenWriteRepo.getActiveTokensByStaffAccountId(staffAccountId);
        if (activeTokens.isEmpty()) {
            log.error("No active refresh tokens found for staff account with ID: {}", staffAccountId.getValue());
            throw new RefreshTokenRevocationServiceException(RefreshTokenRevocationServiceExceptionInfo.noActiveTokensFound());
        }

        activeTokens.forEach(RefreshToken::revoke);

        refreshTokenWriteRepo.updateAll(activeTokens);

        log.info("Successfully revoked {} active refresh token(s) for staff account with ID: {}", activeTokens.size(), staffAccountId.getValue());
    }
}
