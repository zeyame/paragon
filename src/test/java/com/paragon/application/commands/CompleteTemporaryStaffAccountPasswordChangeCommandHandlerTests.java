package com.paragon.application.commands;

import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommand;
import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommandHandler;
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
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class CompleteTemporaryStaffAccountPasswordChangeCommandHandlerTests {
    private final CompleteTemporaryStaffAccountPasswordChangeCommandHandler sut;
    private final CompleteTemporaryStaffAccountPasswordChangeCommand command;
    private StaffAccountWriteRepo staffAccountWriteRepoMock;
    private UnitOfWork unitOfWorkMock;
    private PasswordHasher passwordHasherMock;
    private AppExceptionHandler appExceptionHandlerMock;

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        unitOfWorkMock = mock(UnitOfWork.class);
        passwordHasherMock = mock(PasswordHasher.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        sut = new CompleteTemporaryStaffAccountPasswordChangeCommandHandler(staffAccountWriteRepoMock, unitOfWorkMock, passwordHasherMock, appExceptionHandlerMock);

        // set up happy case
        StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();
        command = new CompleteTemporaryStaffAccountPasswordChangeCommand(
                staffAccount.getId().getValue().toString(),
                PlaintextPassword.generate().getValue()
        );
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.of(staffAccount));
        when(passwordHasherMock.verify(command.newPassword(), staffAccount.getPassword()))
                .thenReturn(false);
    }

    @Test
    void shouldBeginTransaction() {
        // When
        sut.handle(command);

        // Then
        verify(unitOfWorkMock, times(1)).begin();
    }

    @Test
    void shouldThrowIfStaffAccountDoesNotExist() {
        // Given
        StaffAccountId staffAccountId = StaffAccountId.from(command.id());
        when(staffAccountWriteRepoMock.getById(staffAccountId))
                .thenReturn(Optional.empty());
        AppException expectedException = new AppException(AppExceptionInfo.staffAccountNotFound(staffAccountId.getValue().toString()));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(expectedException.getMessage(), expectedException.getErrorCode(), expectedException.getStatusCode());
    }

    @Test
    void shouldThrowIfEnteredPasswordIsTheSameAsCurrentOne() {
        // Given
        when(passwordHasherMock.verify(anyString(), any(Password.class)))
                .thenReturn(true);
        AppException expectedException = new AppException(AppExceptionInfo.newPasswordMatchesCurrentPassword());

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(expectedException.getMessage(), expectedException.getErrorCode(), expectedException.getStatusCode());
    }

    @Test
    void shouldCatchDomainException_andTranslateToAppException() {
        // Given
        StaffAccountId staffAccountId = StaffAccountId.generate();
        CompleteTemporaryStaffAccountPasswordChangeCommand invalidCommand = new CompleteTemporaryStaffAccountPasswordChangeCommand(
                staffAccountId.getValue().toString(),
                "new-password"      // invalid password
        );
        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(invalidCommand));
    }

    @Test
    void shouldCatchInfraException_andTranslateToAppException() {
        // Given
        doThrow(InfraException.class)
                .when(staffAccountWriteRepoMock)
                .getById(any(StaffAccountId.class));
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command));
    }
}
