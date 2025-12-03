package com.paragon.application.commands.completetemporarystaffaccountpasswordchange;

import com.paragon.application.commands.CommandHandler;
import com.paragon.application.common.interfaces.UnitOfWork;

public class CompleteTemporaryStaffAccountPasswordChangeCommandHandler implements CommandHandler<CompleteTemporaryStaffAccountPasswordChangeCommand, CompleteTemporaryStaffAccountPasswordChangeResponse> {
    private final UnitOfWork unitOfWork;

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandler(UnitOfWork unitOfWorkMock) {
        this.unitOfWork = unitOfWorkMock;
    }

    @Override
    public CompleteTemporaryStaffAccountPasswordChangeResponse handle(CompleteTemporaryStaffAccountPasswordChangeCommand command) {
        unitOfWork.begin();
        return null;
    }
}
