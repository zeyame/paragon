package com.paragon.application.commands.completetemporarystaffaccountpasswordchange;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.PasswordHasher;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.StaffAccountId;

public class CompleteTemporaryStaffAccountPasswordChangeCommandHandler implements CommandHandler<CompleteTemporaryStaffAccountPasswordChangeCommand, CompleteTemporaryStaffAccountPasswordChangeResponse> {
    private final StaffAccountWriteRepo staffAccountWriteRepo;
    private final UnitOfWork unitOfWork;
    private final PasswordHasher passwordHasher;

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandler(StaffAccountWriteRepo staffAccountWriteRepo, UnitOfWork unitOfWork, PasswordHasher passwordHasher) {
        this.staffAccountWriteRepo = staffAccountWriteRepo;
        this.unitOfWork = unitOfWork;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public CompleteTemporaryStaffAccountPasswordChangeResponse handle(CompleteTemporaryStaffAccountPasswordChangeCommand command) {
        unitOfWork.begin();
        StaffAccount staffAccount = staffAccountWriteRepo.getById(StaffAccountId.from(command.id()))
                .orElseThrow(() -> new AppException(AppExceptionInfo.staffAccountNotFound(command.id())));
        if (isTheSamePassword(command.newPassword(), staffAccount.getPassword())) {
            throw new AppException(AppExceptionInfo.newPasswordMatchesCurrentPassword());
        }
        return null;
    }

    private boolean isTheSamePassword(String newPassword, Password currentPassword) {
        return passwordHasher.verify(newPassword, currentPassword);
    }
}
