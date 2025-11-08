package com.paragon.application.commands.enablestaffaccount;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EnableStaffAccountCommandHandler implements CommandHandler<EnableStaffAccountCommand, EnableStaffAccountCommandResponse> {

    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final UnitOfWork unitOfWork;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(EnableStaffAccountCommandHandler.class);

    public EnableStaffAccountCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo,
                                            UnitOfWork unitOfWork,
                                            EventBus eventBus,
                                            AppExceptionHandler appExceptionHandler) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.unitOfWork = unitOfWork;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public EnableStaffAccountCommandResponse handle(EnableStaffAccountCommand command) {
        unitOfWork.begin();
        try {
            StaffAccountId staffAccountId = StaffAccountId.from(command.staffAccountIdToBeEnabled());
            StaffAccount staffAccount = staffAccountWriteRepo.getById(staffAccountId)
                    .orElseThrow(() -> new AppException(AppExceptionInfo.staffAccountNotFound(command.staffAccountIdToBeEnabled())));

            staffAccount.enable(StaffAccountId.from(command.requestingStaffAccountId()));
            staffAccountWriteRepo.update(staffAccount);

            eventBus.publishAll(staffAccount.dequeueUncommittedEvents());

            unitOfWork.commit();

            log.info("Staff account {} enabled by {}", staffAccountId.getValue(), command.requestingStaffAccountId());

            return new EnableStaffAccountCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    staffAccount.getStatus().toString(),
                    staffAccount.getEnabledBy() != null ? staffAccount.getEnabledBy().getValue().toString() : null,
                    staffAccount.getVersion().getValue()
            );
        } catch (DomainException ex) {
            log.error("Unable to enable staff account {}: domain rule violation - {}",
                    command.staffAccountIdToBeEnabled(), ex.getMessage(), ex);
            unitOfWork.rollback();
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Unable to enable staff account {}: infrastructure error - {}",
                    command.staffAccountIdToBeEnabled(), ex.getMessage(), ex);
            unitOfWork.rollback();
            throw appExceptionHandler.handleInfraException(ex);
        }
    }
}
