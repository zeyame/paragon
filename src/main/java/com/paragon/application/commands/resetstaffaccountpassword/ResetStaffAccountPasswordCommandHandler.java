package com.paragon.application.commands.resetstaffaccountpassword;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.PlaintextPassword;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResetStaffAccountPasswordCommandHandler implements CommandHandler<ResetStaffAccountPasswordCommand, ResetStaffAccountPasswordCommandResponse> {

    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final UnitOfWork unitOfWork;
    private final EventBus eventBus;
    private final AppExceptionHandler appExceptionHandler;
    private final PasswordHasher passwordHasher;
    private static final Logger log = LoggerFactory.getLogger(ResetStaffAccountPasswordCommandHandler.class);

    public ResetStaffAccountPasswordCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo,
                                                   UnitOfWork unitOfWork,
                                                   EventBus eventBus,
                                                   AppExceptionHandler appExceptionHandler,
                                                   PasswordHasher passwordHasher) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.unitOfWork = unitOfWork;
        this.eventBus = eventBus;
        this.appExceptionHandler = appExceptionHandler;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public ResetStaffAccountPasswordCommandResponse handle(ResetStaffAccountPasswordCommand command) {
        unitOfWork.begin();
        try {
            log.info("Resetting password for staff account {}", command.staffAccountIdToReset());

            StaffAccountId staffAccountId = StaffAccountId.from(command.staffAccountIdToReset());
            StaffAccount staffAccount = staffAccountWriteRepo.getById(staffAccountId)
                    .orElseThrow(() -> new AppException(AppExceptionInfo.staffAccountNotFound(command.staffAccountIdToReset())));

            PlaintextPassword tempPassword = PlaintextPassword.generate();
            String hashedTempPassword = passwordHasher.hash(tempPassword.getValue());

            StaffAccountId resetBy = StaffAccountId.from(command.requestingStaffAccountId());
            staffAccount.resetPassword(Password.of(hashedTempPassword), resetBy);
            staffAccountWriteRepo.update(staffAccount);

            eventBus.publishAll(staffAccount.dequeueUncommittedEvents());

            unitOfWork.commit();

            return new ResetStaffAccountPasswordCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    tempPassword.getValue(),
                    staffAccount.getStatus().toString(),
                    staffAccount.getPasswordIssuedAt(),
                    staffAccount.getVersion().getValue()
            );
        } catch (DomainException ex) {
            log.error("Failed to reset password for staff account {} due to domain rule violation: {}",
                    command.staffAccountIdToReset(), ex.getMessage(), ex);
            unitOfWork.rollback();
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Failed to reset password for staff account {} due to infrastructure error: {}",
                    command.staffAccountIdToReset(), ex.getMessage(), ex);
            unitOfWork.rollback();
            throw appExceptionHandler.handleInfraException(ex);
        }
    }
}
