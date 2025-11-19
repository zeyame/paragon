package com.paragon.application.commands.disablestaffaccount;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DisableStaffAccountCommandHandler implements CommandHandler<DisableStaffAccountCommand, DisableStaffAccountCommandResponse> {

    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final UnitOfWork uow;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(DisableStaffAccountCommandHandler.class);

    public DisableStaffAccountCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo,
                                             UnitOfWork uow,
                                             EventBus eventBus,
                                             AppExceptionHandler appExceptionHandler) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.uow = uow;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public DisableStaffAccountCommandResponse handle(DisableStaffAccountCommand command) {
        uow.begin();
        try {
            StaffAccountId staffAccountId = StaffAccountId.from(command.staffAccountIdToBeDisabled());
            StaffAccount staffAccount = staffAccountWriteRepo.getById(staffAccountId)
                    .orElseThrow(() -> new AppException(AppExceptionInfo.staffAccountNotFound(command.staffAccountIdToBeDisabled())));

            staffAccount.disable(StaffAccountId.from(command.requestingStaffAccountId()));
            staffAccountWriteRepo.update(staffAccount);

            eventBus.publishAll(staffAccount.dequeueUncommittedEvents());

            uow.commit();

            log.info("Staff account {} disabled by {}", staffAccountId.getValue(), command.requestingStaffAccountId());

            return new DisableStaffAccountCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    staffAccount.getStatus().toString(),
                    staffAccount.getDisabledBy() != null ? staffAccount.getDisabledBy().getValue().toString() : null,
                    staffAccount.getVersion().getValue()
            );
        } catch (DomainException ex) {
            log.error("Unable to disable staff account {}: domain rule violation - {}",
                    command.staffAccountIdToBeDisabled(), ex.getMessage(), ex);
            uow.rollback();
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Unable to disable staff account {}: infrastructure error - {}",
                    command.staffAccountIdToBeDisabled(), ex.getMessage(), ex);
            uow.rollback();
            throw appExceptionHandler.handleInfraException(ex);
        }
    }
}
