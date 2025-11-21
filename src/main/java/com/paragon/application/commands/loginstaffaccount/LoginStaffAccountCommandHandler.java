package com.paragon.application.commands.loginstaffaccount;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.application.common.interfaces.PasswordHasher;
import com.paragon.application.common.interfaces.TokenHasher;
import com.paragon.domain.interfaces.RefreshTokenWriteRepo;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoginStaffAccountCommandHandler implements CommandHandler<LoginStaffAccountCommand, LoginStaffAccountCommandResponse> {
    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final RefreshTokenWriteRepo refreshTokenWriteRepo;
    private final UnitOfWork unitOfWork;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private final PasswordHasher passwordHasher;
    private final TokenHasher tokenHasher;

    public LoginStaffAccountCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo, RefreshTokenWriteRepo refreshTokenWriteRepo,
                                           UnitOfWork unitOfWork, EventBus eventBus, AppExceptionHandler appExceptionHandler,
                                           PasswordHasher passwordHasher, TokenHasher tokenHasher) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.refreshTokenWriteRepo = refreshTokenWriteRepo;
        this.unitOfWork = unitOfWork;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
        this.passwordHasher = passwordHasher;
        this.tokenHasher = tokenHasher;
    }

    @Override
    public LoginStaffAccountCommandResponse handle(LoginStaffAccountCommand command) {
        unitOfWork.begin();
        StaffAccount staffAccount = null;
        try {
            staffAccount = staffAccountWriteRepo.getByUsername(Username.of(command.username()))
                    .orElseThrow(() -> new AppException(AppExceptionInfo.invalidLoginCredentials()));

            if (!isValidPassword(command.password(), staffAccount.getPassword())) {
                staffAccount.registerFailedLoginAttempt();
                staffAccountWriteRepo.update(staffAccount);
                unitOfWork.commit();
                throw new AppException(AppExceptionInfo.invalidLoginCredentials());
            }

            staffAccount.login();
            staffAccountWriteRepo.update(staffAccount);

            PlaintextRefreshToken plaintextRefreshToken = PlaintextRefreshToken.generate();
            RefreshTokenHash tokenHash = tokenHasher.hash(plaintextRefreshToken);

            RefreshToken refreshToken = RefreshToken.issue(tokenHash, staffAccount.getId(), IpAddress.of(command.ipAddress()));
            refreshTokenWriteRepo.create(refreshToken);

            List<String> permissionCodes = staffAccount.getPermissionCodes()
                    .stream()
                    .map(PermissionCode::getValue)
                    .collect(Collectors.toList());

            unitOfWork.commit();

            log.info("Staff account '{}' (ID: {}) successfully logged in.",
                    staffAccount.getUsername().getValue(),
                    staffAccount.getId().getValue()
            );

            return new LoginStaffAccountCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    staffAccount.getUsername().getValue(),
                    staffAccount.requiresPasswordReset(),
                    plaintextRefreshToken.getValue(),
                    permissionCodes,
                    staffAccount.getVersion().getValue()
            );
        } catch (DomainException ex) {
            log.error("Staff account login failed for username='{}': domain rule violation - {}",
                    command.username(), ex.getMessage(), ex);
            unitOfWork.rollback();
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Staff account login failed for username='{}': infrastructure related error occurred - {}",
                    command.username(), ex.getMessage(), ex);
            unitOfWork.rollback();
            throw appExceptionHandler.handleInfraException(ex);
        } finally {
            if (staffAccount != null) {
                eventBus.publishAll(staffAccount.dequeueUncommittedEvents());
            }
        }
    }

    private boolean isValidPassword(String enteredPassword, Password storedPassword) {
        return passwordHasher.verify(enteredPassword, storedPassword);
    }
}
