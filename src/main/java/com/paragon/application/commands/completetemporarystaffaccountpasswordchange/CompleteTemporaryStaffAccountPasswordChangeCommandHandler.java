package com.paragon.application.commands.completetemporarystaffaccountpasswordchange;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repositories.StaffAccountPasswordHistoryWriteRepo;
import com.paragon.domain.interfaces.StaffAccountPasswordReusePolicy;
import com.paragon.domain.interfaces.repositories.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CompleteTemporaryStaffAccountPasswordChangeCommandHandler implements CommandHandler<CompleteTemporaryStaffAccountPasswordChangeCommand, CompleteTemporaryStaffAccountPasswordChangeCommandResponse> {
    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final StaffAccountPasswordHistoryWriteRepo staffAccountPasswordHistoryWriteRepo;
    private final UnitOfWork unitOfWork;
    private final PasswordHasher passwordHasher;
    private final AppExceptionHandler appExceptionHandler;
    private final EventBus eventBus;
    private final StaffAccountPasswordReusePolicy staffAccountPasswordReusePolicy;
    private static final Logger log = LoggerFactory.getLogger(CompleteTemporaryStaffAccountPasswordChangeCommandHandler.class);

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo,
                                                                     StaffAccountPasswordHistoryWriteRepo staffAccountPasswordHistoryWriteRepo,
                                                                     UnitOfWork unitOfWork, PasswordHasher passwordHasher,
                                                                     AppExceptionHandler appExceptionHandler, EventBus eventBus,
                                                                     StaffAccountPasswordReusePolicy staffAccountPasswordReusePolicy) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.staffAccountPasswordHistoryWriteRepo = staffAccountPasswordHistoryWriteRepo;
        this.unitOfWork = unitOfWork;
        this.passwordHasher = passwordHasher;
        this.appExceptionHandler = appExceptionHandler;
        this.eventBus = eventBus;
        this.staffAccountPasswordReusePolicy = staffAccountPasswordReusePolicy;
    }

    @Override
    public CompleteTemporaryStaffAccountPasswordChangeCommandResponse handle(CompleteTemporaryStaffAccountPasswordChangeCommand command) {
        try {
            log.info("Completing temporary password change for staff account ID: {}", command.id());

            unitOfWork.begin();
            StaffAccount staffAccount = staffAccountWriteRepo.getById(StaffAccountId.from(command.id()))
                    .orElseThrow(() -> new AppException(AppExceptionInfo.staffAccountNotFound(command.id())));

            PlaintextPassword enteredPassword = PlaintextPassword.of(command.newPassword());
            assertNewPasswordIsNotEqualToCurrent(enteredPassword, staffAccount.getPassword());

            StaffAccountPasswordHistory passwordHistory = staffAccountPasswordHistoryWriteRepo.getPasswordHistory(staffAccount.getId());
            staffAccountPasswordReusePolicy.ensureNotViolated(enteredPassword, passwordHistory);

            Password hashedPassword = passwordHasher.hash(PlaintextPassword.of(command.newPassword()));
            staffAccount.completeTemporaryPasswordChange(hashedPassword);
            staffAccountWriteRepo.update(staffAccount);

            PasswordHistoryEntry passwordHistoryEntry = new PasswordHistoryEntry(
                    staffAccount.getId(), hashedPassword, false, DateTimeUtc.now()
            );
            staffAccountPasswordHistoryWriteRepo.appendEntry(passwordHistoryEntry);

            eventBus.publishAll(staffAccount.dequeueUncommittedEvents());
            unitOfWork.commit();

            log.info("Successfully completed temporary password change for staff account '{}' (ID: {})",
                    staffAccount.getUsername().getValue(), staffAccount.getId().getValue());

            return new CompleteTemporaryStaffAccountPasswordChangeCommandResponse(
                    staffAccount.getId().getValue().toString(),
                    staffAccount.getUsername().getValue(),
                    staffAccount.getStatus().toString(),
                    staffAccount.getVersion().getValue()
            );
        } catch (DomainException ex) {
            log.error("Failed to complete temporary password change for staff account ID '{}' due to domain rule violation: {}",
                    command.id(), ex.getMessage(), ex);
            unitOfWork.rollback();
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            log.error("Failed to complete temporary password change for staff account ID '{}' due to infrastructure error: {}",
                    command.id(), ex.getMessage(), ex);
            unitOfWork.rollback();
            throw appExceptionHandler.handleInfraException(ex);
        }
    }

    private void assertNewPasswordIsNotEqualToCurrent(PlaintextPassword newPassword, Password currentPassword) {
        if (passwordHasher.verify(newPassword.getValue(), currentPassword)) {
            throw new AppException(AppExceptionInfo.newPasswordMatchesCurrentPassword());
        }
    }
}
