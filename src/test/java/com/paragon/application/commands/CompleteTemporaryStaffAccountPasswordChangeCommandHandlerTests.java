package com.paragon.application.commands;

import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommand;
import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommandHandler;
import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.domain.interfaces.services.PasswordHasher;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repositories.StaffAccountPasswordHistoryWriteRepo;
import com.paragon.domain.interfaces.services.StaffAccountPasswordReusePolicy;
import com.paragon.domain.interfaces.repositories.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.helpers.fixtures.StaffAccountPasswordHistoryFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CompleteTemporaryStaffAccountPasswordChangeCommandHandlerTests {
    private final CompleteTemporaryStaffAccountPasswordChangeCommandHandler sut;
    private StaffAccountWriteRepo staffAccountWriteRepoMock;
    private UnitOfWork unitOfWorkMock;
    private PasswordHasher passwordHasherMock;
    private AppExceptionHandler appExceptionHandlerMock;
    private EventBus eventBusMock;
    private final CompleteTemporaryStaffAccountPasswordChangeCommand command;
    private final StaffAccount staffAccount;
    private final Password hashedPassword;
    private StaffAccountPasswordHistoryWriteRepo staffAccountPasswordHistoryWriteRepoMock;
    private StaffAccountPasswordReusePolicy staffAccountPasswordReusePolicyMock;

    public CompleteTemporaryStaffAccountPasswordChangeCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        staffAccountPasswordHistoryWriteRepoMock = mock(StaffAccountPasswordHistoryWriteRepo.class);
        unitOfWorkMock = mock(UnitOfWork.class);
        passwordHasherMock = mock(PasswordHasher.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        eventBusMock = mock(EventBus.class);
        staffAccountPasswordReusePolicyMock = mock(StaffAccountPasswordReusePolicy.class);
        sut = new CompleteTemporaryStaffAccountPasswordChangeCommandHandler(
                staffAccountWriteRepoMock, staffAccountPasswordHistoryWriteRepoMock, unitOfWorkMock,
                passwordHasherMock, appExceptionHandlerMock, eventBusMock, staffAccountPasswordReusePolicyMock);

        // set up happy case
        staffAccount = StaffAccountFixture.validStaffAccount();
        command = new CompleteTemporaryStaffAccountPasswordChangeCommand(
                staffAccount.getId().getValue().toString(),
                PlaintextPassword.generate().getValue()
        );
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.of(staffAccount));
        when(passwordHasherMock.verify(command.newPassword(), staffAccount.getPassword()))
                .thenReturn(false);

        hashedPassword = Password.of("hashed-password");
        when(passwordHasherMock.hash(any(PlaintextPassword.class)))
                .thenReturn(hashedPassword);

        when(staffAccountPasswordHistoryWriteRepoMock.getPasswordHistory(any(StaffAccountId.class)))
                .thenReturn(StaffAccountPasswordHistoryFixture.validHistoryForStaffAccount(staffAccount.getId()));
    }

    @Test
    void shouldBeginTransaction() {
        // When
        sut.handle(command);

        // Then
        verify(unitOfWorkMock, times(1)).begin();
    }

    @Test
    void shouldCommitTransaction() {
        // When
        sut.handle(command);

        // Then
        verify(unitOfWorkMock, times(1)).commit();
    }

    @Test
    void shouldCompleteTemporaryStaffAccountPasswordChange() {
        // When
        sut.handle(command);

        // Then
        ArgumentCaptor<StaffAccount> staffAccountArgumentCaptor = ArgumentCaptor.forClass(StaffAccount.class);
        verify(staffAccountWriteRepoMock, times(1)).update(staffAccountArgumentCaptor.capture());
        StaffAccount capturedStaffAccount = staffAccountArgumentCaptor.getValue();
        assertThat(capturedStaffAccount.isPasswordTemporary()).isFalse();
    }

    @Test
    void shouldAppendNewEntryToStaffAccountPasswordHistory() {
        // When
        sut.handle(command);

        // Then
        ArgumentCaptor<PasswordHistoryEntry> passwordHistoryEntryCaptor = ArgumentCaptor.forClass(PasswordHistoryEntry.class);
        verify(staffAccountPasswordHistoryWriteRepoMock, times(1))
                .appendEntry(passwordHistoryEntryCaptor.capture());
        PasswordHistoryEntry passwordHistoryEntry = passwordHistoryEntryCaptor.getValue();
        assertThat(passwordHistoryEntry.staffAccountId()).isEqualTo(staffAccount.getId());
        assertThat(passwordHistoryEntry.hashedPassword()).isEqualTo(hashedPassword);
        assertThat(passwordHistoryEntry.isTemporary()).isFalse();
        assertThat(passwordHistoryEntry.changedAt()).isNotNull();
    }

    @Test
    void shouldReturnExpectedCommandResponse() {
        // When
        CompleteTemporaryStaffAccountPasswordChangeCommandResponse commandResponse = sut.handle(command);

        // Then
        assertThat(commandResponse.id()).isEqualTo(staffAccount.getId().getValue().toString());
        assertThat(commandResponse.username()).isEqualTo(staffAccount.getUsername().getValue());
        assertThat(commandResponse.status()).isEqualTo("ACTIVE");
        assertThat(commandResponse.version()).isEqualTo(2);
    }

    @Test
    void shouldCheckPasswordReusePolicy() {
        // When
        sut.handle(command);

        // Then
        ArgumentCaptor<PlaintextPassword> passwordCaptor = ArgumentCaptor.forClass(PlaintextPassword.class);
        ArgumentCaptor<StaffAccountPasswordHistory> passwordHistoryCaptor = ArgumentCaptor.forClass(StaffAccountPasswordHistory.class);
        verify(staffAccountPasswordReusePolicyMock, times(1))
                .ensureNotViolated(passwordCaptor.capture(), passwordHistoryCaptor.capture());

        PlaintextPassword plaintextPassword = passwordCaptor.getValue();
        StaffAccountPasswordHistory passwordHistory = passwordHistoryCaptor.getValue();
        assertThat(plaintextPassword.getValue()).isEqualTo(command.newPassword());
        assertThat(passwordHistory).isNotNull();
        assertThat(passwordHistory.entries()).isNotEmpty();
    }

    @Test
    void shouldAppendNewPasswordToStaffAccountPasswordHistory() {
        // When
        sut.handle(command);

        // Then
        ArgumentCaptor<PasswordHistoryEntry> passwordHistoryEntryCaptor = ArgumentCaptor.forClass(PasswordHistoryEntry.class);
        verify(staffAccountPasswordHistoryWriteRepoMock, times(1))
                .appendEntry(passwordHistoryEntryCaptor.capture());
        PasswordHistoryEntry passwordHistoryEntry = passwordHistoryEntryCaptor.getValue();
        assertThat(passwordHistoryEntry.staffAccountId()).isEqualTo(staffAccount.getId());
        assertThat(passwordHistoryEntry.hashedPassword()).isEqualTo(hashedPassword);
        assertThat(passwordHistoryEntry.isTemporary()).isFalse();
    }

    @Test
    void shouldPublishDomainEvents() {
        // When
        sut.handle(command);

        // Then
        ArgumentCaptor<List<DomainEvent>> domainEventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventBusMock, times(1)).publishAll(domainEventsCaptor.capture());
        List<DomainEvent> capturedDomainEvents = domainEventsCaptor.getValue();
        assertThat(capturedDomainEvents).isNotEmpty();
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
    void whenDomainExceptionIsThrown_shouldRollbackTransaction_andTranslateToAppException() {
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
        verify(unitOfWorkMock, times(1)).rollback();
    }

    @Test
    void whenInfraExceptionIsThrown_shouldRollbackTransaction_andTranslateToAppException() {
        // Given
        doThrow(InfraException.class)
                .when(staffAccountWriteRepoMock)
                .getById(any(StaffAccountId.class));
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command));
        verify(unitOfWorkMock, times(1)).rollback();
    }
}
