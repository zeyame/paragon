package com.paragon.application.commands.refreshstaffaccounttoken;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.TokenHasher;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.services.StaffAccountRefreshTokenRevocationService;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.RefreshTokenWriteRepo;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.PlaintextRefreshToken;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.exceptions.InfraExceptionHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RefreshStaffAccountTokenCommandHandler implements CommandHandler<RefreshStaffAccountTokenCommand, RefreshStaffAccountTokenCommandResponse> {
    private final RefreshTokenWriteRepo refreshTokenWriteRepo;
    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final StaffAccountRefreshTokenRevocationService tokenRevocationService;
    private final UnitOfWork unitOfWork;
    private final TokenHasher tokenHasher;
    private final AppExceptionHandler appExceptionHandler;
    private final InfraExceptionHandler infraExceptionHandler;

    public RefreshStaffAccountTokenCommandHandler(RefreshTokenWriteRepo refreshTokenWriteRepo, StaffAccountWriteRepo staffAccountWriteRepo,
                                                  StaffAccountRefreshTokenRevocationService tokenRevocationService, UnitOfWork unitOfWork,
                                                  TokenHasher tokenHasher, AppExceptionHandler appExceptionHandler,
                                                  InfraExceptionHandler infraExceptionHandler) {
        this.refreshTokenWriteRepo = refreshTokenWriteRepo;
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.tokenRevocationService = tokenRevocationService;
        this.unitOfWork = unitOfWork;
        this.tokenHasher = tokenHasher;
        this.appExceptionHandler = appExceptionHandler;
        this.infraExceptionHandler = infraExceptionHandler;
    }


    @Override
    public RefreshStaffAccountTokenCommandResponse handle(RefreshStaffAccountTokenCommand command) {
        try {
            unitOfWork.begin();

            RefreshToken refreshToken = loadAndValidateRefreshToken(command.plainToken());

            PlaintextRefreshToken newPlainToken = PlaintextRefreshToken.generate();
            RefreshTokenHash newTokenHash = tokenHasher.hash(newPlainToken);
            RefreshToken newToken = refreshToken.replace(newTokenHash);

            refreshTokenWriteRepo.update(refreshToken);
            refreshTokenWriteRepo.create(newToken);

            Optional<StaffAccount> optionalStaffAccount = staffAccountWriteRepo.getById(refreshToken.getStaffAccountId());
            if (optionalStaffAccount.isEmpty()) {
                unitOfWork.rollback();
                throw new AppException(AppExceptionInfo.staffAccountNotFound(refreshToken.getStaffAccountId().getValue().toString()));
            }

            StaffAccount staffAccount = optionalStaffAccount.get();
            List<String> permissionCodes = staffAccount.getPermissionCodes()
                    .stream()
                    .map(PermissionCode::getValue)
                    .toList();

            unitOfWork.commit();

            return new RefreshStaffAccountTokenCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    staffAccount.getUsername().getValue(),
                    staffAccount.requiresPasswordReset(),
                    newPlainToken.getValue(),
                    permissionCodes,
                    staffAccount.getVersion().getValue()
            );
        } catch (DomainException ex) {
            unitOfWork.rollback();
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            unitOfWork.rollback();
            throw appExceptionHandler.handleInfraException(ex);
        }
    }

    private RefreshToken loadAndValidateRefreshToken(String plainToken) {
        RefreshTokenHash tokenHash = tokenHasher.hash(PlaintextRefreshToken.of(plainToken));
        RefreshToken refreshToken = refreshTokenWriteRepo.getByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(AppExceptionInfo.invalidRefreshToken()));

        if (refreshToken.isExpired()) {
            throw new AppException(AppExceptionInfo.invalidRefreshToken());
        }

        if (refreshToken.isRevoked()) {
            // possible suspicious activity
            tokenRevocationService.revokeAllTokensForStaffAccount(refreshToken.getStaffAccountId());
            unitOfWork.commit();
            throw new AppException(AppExceptionInfo.invalidRefreshToken());
        }
        return refreshToken;
    }
}
