package com.paragon.application.commands.completetemporarystaffaccountpasswordchange;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.PasswordHasher;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.StaffAccountPasswordHistoryWriteRepo;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.infrastructure.persistence.exceptions.InfraException;

public class CompleteTemporaryStaffAccountPasswordChangeCommandHandler implements CommandHandler<CompleteTemporaryStaffAccountPasswordChangeCommand, CompleteTemporaryStaffAccountPasswordChangeResponse> {
    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final StaffAccountPasswordHistoryWriteRepo staffAccountPasswordHistoryWriteRepo;
    private final UnitOfWork unitOfWork;
    private final PasswordHasher passwordHasher;
    private final AppExceptionHandler appExceptionHandler;
    private final EventBus eventBus;

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo,
                                                                     StaffAccountPasswordHistoryWriteRepo staffAccountPasswordHistoryWriteRepo,
                                                                     UnitOfWork unitOfWork, PasswordHasher passwordHasher,
                                                                     AppExceptionHandler appExceptionHandler, EventBus eventBus
    ) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.staffAccountPasswordHistoryWriteRepo = staffAccountPasswordHistoryWriteRepo;
        this.unitOfWork = unitOfWork;
        this.passwordHasher = passwordHasher;
        this.appExceptionHandler = appExceptionHandler;
        this.eventBus = eventBus;
    }

    @Override
    public CompleteTemporaryStaffAccountPasswordChangeResponse handle(CompleteTemporaryStaffAccountPasswordChangeCommand command) {
        try {
            unitOfWork.begin();
            StaffAccount staffAccount = staffAccountWriteRepo.getById(StaffAccountId.from(command.id()))
                    .orElseThrow(() -> new AppException(AppExceptionInfo.staffAccountNotFound(command.id())));
            if (passwordsAreEqual(command.newPassword(), staffAccount.getPassword())) {
                throw new AppException(AppExceptionInfo.newPasswordMatchesCurrentPassword());
            }

            Password hashedPassword = passwordHasher.hash(PlaintextPassword.of(command.newPassword()));
            staffAccount.completeTemporaryPasswordChange(hashedPassword);
            staffAccountWriteRepo.update(staffAccount);

            PasswordHistoryEntry passwordHistoryEntry = new PasswordHistoryEntry(
                    staffAccount.getId(), hashedPassword, false, DateTimeUtc.now()
            );
            staffAccountPasswordHistoryWriteRepo.appendEntry(passwordHistoryEntry);

            eventBus.publishAll(staffAccount.dequeueUncommittedEvents());
            unitOfWork.commit();
        } catch (DomainException ex) {
            throw appExceptionHandler.handleDomainException(ex);
        } catch (InfraException ex) {
            throw appExceptionHandler.handleInfraException(ex);
        }
        return null;
    }

    private boolean passwordsAreEqual(String newPassword, Password currentPassword) {
        return passwordHasher.verify(newPassword, currentPassword);
    }
}
