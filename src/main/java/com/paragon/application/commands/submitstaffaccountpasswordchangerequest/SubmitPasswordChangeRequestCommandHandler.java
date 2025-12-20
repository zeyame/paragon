package com.paragon.application.commands.submitstaffaccountpasswordchangerequest;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.events.EventBus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repositories.StaffAccountRequestWriteRepo;
import com.paragon.domain.interfaces.repositories.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SubmitPasswordChangeRequestCommandHandler implements CommandHandler<SubmitPasswordChangeRequestCommand, SubmitPasswordChangeRequestCommandResponse> {

    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final StaffAccountRequestWriteRepo staffAccountRequestWriteRepo;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private static final Logger log = LoggerFactory.getLogger(SubmitPasswordChangeRequestCommandHandler.class);

    public SubmitPasswordChangeRequestCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo,
                                                      StaffAccountRequestWriteRepo staffAccountRequestWriteRepo,
                                                      EventBus eventBus,
                                                      AppExceptionHandler appExceptionHandler) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.staffAccountRequestWriteRepo = staffAccountRequestWriteRepo;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
    }

    @Override
    public SubmitPasswordChangeRequestCommandResponse handle(SubmitPasswordChangeRequestCommand command) {
        try {
            StaffAccountId staffAccountId = StaffAccountId.from(command.staffAccountId());
            StaffAccount staffAccount = staffAccountWriteRepo.getById(staffAccountId)
                    .orElseThrow(() -> new AppException(AppExceptionInfo.staffAccountNotFound(command.staffAccountId())));

            boolean hasPendingRequest = staffAccountRequestWriteRepo.existsPendingRequestBySubmitterAndType(
                    staffAccountId,
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            if (hasPendingRequest) {
                throw new AppException(AppExceptionInfo.pendingStaffAccountRequestAlreadyExists(
                        staffAccount.getUsername().getValue(),
                        StaffAccountRequestType.PASSWORD_CHANGE.toString()
                ));
            }

            StaffAccountRequest request = StaffAccountRequest.submit(
                    staffAccountId,
                    StaffAccountRequestType.PASSWORD_CHANGE,
                    null,
                    null
            );

            staffAccountRequestWriteRepo.create(request);

            eventBus.publishAll(request.dequeueUncommittedEvents());

            log.info("Password change request submitted for staff account {}", staffAccountId.getValue());

            return new SubmitPasswordChangeRequestCommandResponse(
                    request.getId().getValue().toString(),
                    request.getSubmittedBy().getValue().toString(),
                    request.getRequestType().toString(),
                    request.getStatus().toString(),
                    request.getSubmittedAt().getValue().toString(),
                    request.getExpiresAt().getValue().toString()
            );
        } catch (DomainException ex) {
            log.error("Unable to submit password change request for staff account {}: domain rule violation - {}",
                    command.staffAccountId(), ex.getMessage(), ex);
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Unable to submit password change request for staff account {}: infrastructure error - {}",
                    command.staffAccountId(), ex.getMessage(), ex);
            throw appExceptionHandler.handleInfraException(ex);
        }
    }
}