package com.paragon.application.commands.completetemporarystaffaccountpasswordchange;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.StaffAccountId;

public class CompleteTemporaryStaffAccountPasswordChangeCommandHandler implements CommandHandler<CompleteTemporaryStaffAccountPasswordChangeCommand, CompleteTemporaryStaffAccountPasswordChangeResponse> {
    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private final UnitOfWork unitOfWork;

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandler(StaffAccountWriteRepo staffAccountWriteRepoMock, UnitOfWork unitOfWorkMock) {
        this.staffAccountWriteRepoMock = staffAccountWriteRepoMock;
        this.unitOfWork = unitOfWorkMock;
    }

    @Override
    public CompleteTemporaryStaffAccountPasswordChangeResponse handle(CompleteTemporaryStaffAccountPasswordChangeCommand command) {
        unitOfWork.begin();
        StaffAccount staffAccount = staffAccountWriteRepoMock.getById(StaffAccountId.from(command.id()))
                .orElseThrow(() -> new AppException(AppExceptionInfo.staffAccountNotFound(command.id())));
        return null;
    }
}
