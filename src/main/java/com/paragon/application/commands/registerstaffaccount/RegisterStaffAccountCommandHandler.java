package com.paragon.application.commands.registerstaffaccount;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RegisterStaffAccountCommandHandler implements CommandHandler<RegisterStaffAccountCommand, RegisterStaffAccountCommandResponse> {

    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private final PasswordHasher passwordHasher;
    private static final Logger log = LoggerFactory.getLogger(RegisterStaffAccountCommandHandler.class);

    public RegisterStaffAccountCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo,
                                              EventBus eventBus, AppExceptionHandler appExceptionHandler,
                                              PasswordHasher passwordHasher
    ) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public RegisterStaffAccountCommandResponse handle(RegisterStaffAccountCommand command) {
        try {
            assertUniqueUsername(command.username());

            StaffAccount staffAccount = StaffAccount.register(
                    Username.of(command.username()),
                    command.email() != null ? Email.of(command.email()) : null,
                    Password.fromPlainText(command.tempPassword(), passwordHasher),
                    OrderAccessDuration.from(command.orderAccessDuration()),
                    ModmailTranscriptAccessDuration.from(command.modmailTranscriptAccessDuration()),
                    StaffAccountId.from(command.createdBy()),
                    command.permissionCodes().stream().map(PermissionCode::of).collect(Collectors.toSet())
            );
            staffAccountWriteRepo.create(staffAccount);

            eventBus.publishAll(staffAccount.dequeueUncommittedEvents());

            log.info(
                    "Staff account registered: id={}, username={}, status={}, registeredBy={}",
                    staffAccount.getId().getValue(),
                    staffAccount.getUsername().getValue(),
                    staffAccount.getStatus(),
                    staffAccount.getCreatedBy().getValue()
            );

            return new RegisterStaffAccountCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    staffAccount.getUsername().getValue(),
                    staffAccount.getStatus().toString(),
                    staffAccount.getVersion().getValue()
            );
        } catch (DomainException ex) {
            log.error("Staff account registration failed for createdBy={}: domain rule violation - {}",
                    command.createdBy(), ex.getMessage(), ex);
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Staff account registration failed for createdBy={}: infrastructure related error occurred - {}",
                    command.createdBy(), ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }

    private void assertUniqueUsername(String username) {
        Optional<StaffAccount> optional = staffAccountWriteRepo.getByUsername(Username.of(username));
        if (optional.isPresent()) {
            throw new AppException(AppExceptionInfo.staffAccountUsernameAlreadyExists(username));
        }
    }
}
