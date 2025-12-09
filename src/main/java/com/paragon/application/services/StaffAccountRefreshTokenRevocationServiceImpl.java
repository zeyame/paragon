package com.paragon.application.services;

import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repositories.RefreshTokenWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffAccountRefreshTokenRevocationServiceImpl implements StaffAccountRefreshTokenRevocationService {
    private final RefreshTokenWriteRepo refreshTokenWriteRepo;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountRefreshTokenRevocationServiceImpl.class);

    public StaffAccountRefreshTokenRevocationServiceImpl(RefreshTokenWriteRepo refreshTokenWriteRepo, AppExceptionHandler appExceptionHandler) {
        this.refreshTokenWriteRepo = refreshTokenWriteRepo;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public void revokeAllTokensForStaffAccount(StaffAccountId staffAccountId) {
        try {
            List<RefreshToken> activeTokens = refreshTokenWriteRepo.getActiveTokensByStaffAccountId(staffAccountId);
            activeTokens.forEach(RefreshToken::revoke);
            refreshTokenWriteRepo.updateAll(activeTokens);
        } catch (DomainException ex) {
            log.error("Domain rule violation while revoking refresh tokens for staff account with ID={}. Error reason: {}",
                    staffAccountId.getValue(), ex.getMessage(), ex);
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Infrastructure related error occurred while revoking refresh tokens for staff account with ID={}. Error reason: {}",
                    staffAccountId.getValue(), ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }
}
