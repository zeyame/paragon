package com.paragon.application.commands.registerstaffaccount;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.context.ActorContext;
import com.paragon.application.events.EventBus;
import com.paragon.application.queries.repositoryinterfaces.PermissionReadRepo;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
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
    private final ActorContext actorContext;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(RegisterStaffAccountCommandHandler.class);

    public RegisterStaffAccountCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo, ActorContext actorContext,
                                              EventBus eventBus, AppExceptionHandler appExceptionHandler
    ) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.actorContext = actorContext;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public RegisterStaffAccountCommandResponse handle(RegisterStaffAccountCommand command) {
        String requestingStaffId = actorContext.getActorId();
        try {
            Optional<StaffAccount> optional = staffAccountWriteRepo.getById(StaffAccountId.from(requestingStaffId));
            if (optional.isEmpty()) {
                log.error("Staff account registration failed: requestingStaffId='{}' does not exist.", requestingStaffId);
                throw new AppException(AppExceptionInfo.staffAccountNotFound(requestingStaffId));
            }
            StaffAccount requestingStaffAccount = optional.get();

            if (!requestingStaffAccount.canRegisterOtherStaffAccounts()) {
                log.warn("Staff account registration request denied: requestingStaffId='{}' lacked MANAGE_ACCOUNTS permission.", requestingStaffId);
                throw new AppException(AppExceptionInfo.permissionAccessDenied("registration"));
            }

            StaffAccount staffAccount = StaffAccount.register(
                    Username.of(command.username()),
                    command.email() != null ? Email.of(command.email()) : null,
                    Password.of(command.tempPassword()),
                    OrderAccessDuration.from(command.orderAccessDuration()),
                    ModmailTranscriptAccessDuration.from(command.modmailTranscriptAccessDuration()),
                    requestingStaffAccount.getId(),
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
            log.error("Staff account registration failed for requestingStaffId={}: domain rule violation - {}",
                    requestingStaffId, ex.getMessage(), ex);
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Staff account registration failed for requestingStaffId={}: infrastructure related error occurred - {}",
                    requestingStaffId, ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }
}
