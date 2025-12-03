package com.paragon.application.commands;

import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommand;
import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommandHandler;
import com.paragon.application.common.interfaces.UnitOfWork;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class CompleteTemporaryStaffAccountPasswordChangeCommandHandlerTests {
    private final CompleteTemporaryStaffAccountPasswordChangeCommandHandler sut;
    private UnitOfWork unitOfWorkMock;

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandlerTests() {
        unitOfWorkMock = mock(UnitOfWork.class);
        sut = new CompleteTemporaryStaffAccountPasswordChangeCommandHandler(unitOfWorkMock);
    }

    @Test
    void shouldBeginTransaction() {
        // When
        sut.handle(new CompleteTemporaryStaffAccountPasswordChangeCommand("new-password"));

        // Then
        verify(unitOfWorkMock, times(1)).begin();
    }
}
