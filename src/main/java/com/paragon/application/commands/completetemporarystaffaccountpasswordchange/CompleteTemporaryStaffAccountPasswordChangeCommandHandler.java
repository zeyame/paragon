package com.paragon.application.commands.completetemporarystaffaccountpasswordchange;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.PasswordHasher;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.PlaintextPassword;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;

public class CompleteTemporaryStaffAccountPasswordChangeCommandHandler implements CommandHandler<CompleteTemporaryStaffAccountPasswordChangeCommand, CompleteTemporaryStaffAccountPasswordChangeResponse> {
    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final UnitOfWork unitOfWork;
    private final PasswordHasher passwordHasher;
    private final AppExceptionHandler appExceptionHandler;

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo, UnitOfWork unitOfWork, PasswordHasher passwordHasher, AppExceptionHandler appExceptionHandler) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.unitOfWork = unitOfWork;
        this.passwordHasher = passwordHasher;
        this.appExceptionHandler = appExceptionHandler;
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
