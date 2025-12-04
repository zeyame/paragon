package com.paragon.application.commands;

import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommand;
import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.repos.StaffAccountWriteRepoImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class CompleteTemporaryStaffAccountPasswordChangeCommandHandlerTests {
    private final CompleteTemporaryStaffAccountPasswordChangeCommandHandler sut;
    private StaffAccountWriteRepo staffAccountWriteRepoMock;
    private UnitOfWork unitOfWorkMock;

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        unitOfWorkMock = mock(UnitOfWork.class);
        sut = new CompleteTemporaryStaffAccountPasswordChangeCommandHandler(staffAccountWriteRepoMock, unitOfWorkMock);
    }

    @Test
    void shouldBeginTransaction() {
        // When
        sut.handle(new CompleteTemporaryStaffAccountPasswordChangeCommand(UUID.randomUUID().toString(), "new-password"));

        // Then
        verify(unitOfWorkMock, times(1)).begin();
    }

    @Test
    void shouldThrowIfStaffAccountDoesNotExist() {
        // Given
        StaffAccountId staffAccountId = StaffAccountId.generate();
        CompleteTemporaryStaffAccountPasswordChangeCommand command = new CompleteTemporaryStaffAccountPasswordChangeCommand(
                staffAccountId.getValue().toString(),
                "new-password"
        );
        when(staffAccountWriteRepoMock.getById(staffAccountId))
                .thenReturn(Optional.empty());
        AppException expectedException = new AppException(AppExceptionInfo.staffAccountNotFound(staffAccountId.getValue().toString()));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(expectedException.getMessage(), expectedException.getErrorCode(), expectedException.getStatusCode());
    }
}
