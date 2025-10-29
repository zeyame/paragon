package com.paragon.application.commands.loginstaffaccount;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.interfaces.TokenHasher;
import com.paragon.domain.interfaces.repos.RefreshTokenWriteRepo;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.IpAddress;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoginStaffAccountCommandHandler implements CommandHandler<LoginStaffAccountCommand, LoginStaffAccountCommandResponse> {
    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final RefreshTokenWriteRepo refreshTokenWriteRepo;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private final PasswordHasher passwordHasher;
    private final TokenHasher tokenHasher;

    public LoginStaffAccountCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo, RefreshTokenWriteRepo refreshTokenWriteRepo,
                                           EventBus eventBus, AppExceptionHandler appExceptionHandler, PasswordHasher passwordHasher,
                                           TokenHasher tokenHasher) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.refreshTokenWriteRepo = refreshTokenWriteRepo;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
        this.passwordHasher = passwordHasher;
        this.tokenHasher = tokenHasher;
    }

    @Override
    public LoginStaffAccountCommandResponse handle(LoginStaffAccountCommand command) {
        StaffAccount staffAccount = null;
        try {
            Optional<StaffAccount> optionalStaffAccount = staffAccountWriteRepo.getByUsername(Username.of(command.username()));
            if (optionalStaffAccount.isEmpty()) {
                throw new AppException(AppExceptionInfo.invalidLoginCredentials());
            }
            staffAccount = optionalStaffAccount.get();

            staffAccount.login(Password.fromPlainText(command.password(), passwordHasher));
            staffAccountWriteRepo.update(staffAccount);

            RefreshToken refreshToken = RefreshToken.issue(staffAccount.getId(), IpAddress.of(command.ipAddress()), tokenHasher);
            refreshTokenWriteRepo.create(refreshToken);

            Set<String> permissionCodes = staffAccount.getPermissionCodes()
                    .stream()
                    .map(PermissionCode::getValue)
                    .collect(Collectors.toSet());

            log.info("Staff account '{}' (ID: {}) successfully logged in.",
                    staffAccount.getUsername().getValue(),
                    staffAccount.getId().getValue()
            );

            return new LoginStaffAccountCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    staffAccount.getUsername().getValue(),
                    staffAccount.requiresPasswordReset(),
                    refreshToken.getTokenHash().getPlainValue(),
                    permissionCodes,
                    staffAccount.getVersion().getValue()
            );
        } catch (DomainException ex) {
            log.error("Staff account login failed for username='{}': domain rule violation - {}",
                    command.username(), ex.getMessage(), ex);
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Staff account login failed for username='{}': infrastructure related error occurred - {}",
                    command.username(), ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        } finally {
            if (staffAccount != null) {
                eventBus.publishAll(staffAccount.dequeueUncommittedEvents());
            }
        }
    }
}
