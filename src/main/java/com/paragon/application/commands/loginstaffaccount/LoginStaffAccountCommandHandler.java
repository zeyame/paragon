package com.paragon.application.commands.loginstaffaccount;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class LoginStaffAccountCommandHandler implements CommandHandler<LoginStaffAccountCommand, LoginStaffAccountCommandResponse> {
    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private final PasswordHasher passwordHasher;

    public LoginStaffAccountCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo, EventBus eventBus,
                                          AppExceptionHandler appExceptionHandler, PasswordHasher passwordHasher) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public LoginStaffAccountCommandResponse handle(LoginStaffAccountCommand command) {
        try {
            Optional<StaffAccount> optionalStaffAccount = staffAccountWriteRepo.getByUsername(Username.of(command.username()));
            if (optionalStaffAccount.isEmpty()) {
                throw new AppException(AppExceptionInfo.invalidLoginCredentials());
            }

            StaffAccount staffAccount = optionalStaffAccount.get();
            staffAccount.login(command.password(), passwordHasher);

            staffAccountWriteRepo.update(staffAccount);

            eventBus.publishAll(staffAccount.dequeueUncommittedEvents());

            log.info("Staff account '{}' (ID: {}) successfully logged in.",
                    staffAccount.getUsername().getValue(),
                    staffAccount.getId().getValue()
            );

            return new LoginStaffAccountCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    staffAccount.getUsername().getValue(),
                    staffAccount.requiresPasswordReset(),
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
        }
    }
}
