package com.paragon.application.commands.logoutstaffaccount;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.TokenHasher;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repositories.RefreshTokenWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.PlaintextRefreshToken;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogoutStaffAccountCommandHandler implements CommandHandler<LogoutStaffAccountCommand, Void> {
    private final RefreshTokenWriteRepo refreshTokenWriteRepo;
    private final TokenHasher tokenHasher;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(LogoutStaffAccountCommandHandler.class);

    public LogoutStaffAccountCommandHandler(RefreshTokenWriteRepo refreshTokenWriteRepo,
                                            TokenHasher tokenHasher,
                                            AppExceptionHandler appExceptionHandler) {
        this.refreshTokenWriteRepo = refreshTokenWriteRepo;
        this.tokenHasher = tokenHasher;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public Void handle(LogoutStaffAccountCommand command) {
        try {
            RefreshToken refreshToken = loadRefreshToken(command.plainToken());

            refreshToken.revoke();
            refreshTokenWriteRepo.update(refreshToken);

            return null;
        } catch (DomainException ex) {
            log.error("Domain rule violation encountered while logging out staff account: {}", ex.getMessage(), ex);
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Infrastructure error occurred while logging out staff account: {}", ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }

    private RefreshToken loadRefreshToken(String plainToken) {
        RefreshTokenHash tokenHash = tokenHasher.hash(PlaintextRefreshToken.of(plainToken));
        return refreshTokenWriteRepo.getByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(AppExceptionInfo.invalidRefreshToken()));
    }
}
