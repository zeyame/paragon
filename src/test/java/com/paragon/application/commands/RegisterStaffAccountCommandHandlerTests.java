package com.paragon.application.commands;

import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.application.common.interfaces.PasswordHasher;
import com.paragon.domain.interfaces.StaffAccountPasswordHistoryWriteRepo;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RegisterStaffAccountCommandHandlerTests {
    private final RegisterStaffAccountCommandHandler sut;
    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private StaffAccountPasswordHistoryWriteRepo staffAccountPasswordHistoryWriteRepoMock;
    private final UnitOfWork uowMock;
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final PasswordHasher passwordHasherMock;
    private final RegisterStaffAccountCommand command;
    private final String hashedPassword;

    public RegisterStaffAccountCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        staffAccountPasswordHistoryWriteRepoMock = mock(StaffAccountPasswordHistoryWriteRepo.class);
        uowMock = mock(UnitOfWork.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        passwordHasherMock = mock(PasswordHasher.class);

        sut = new RegisterStaffAccountCommandHandler(
                staffAccountWriteRepoMock, staffAccountPasswordHistoryWriteRepoMock, uowMock, eventBusMock,
                appExceptionHandlerMock, passwordHasherMock
        );

        command = createValidRegisterStaffAccountCommand();

        hashedPassword = "$2a$10$7eqJtq98hPqEX7fNZaFWoOaYp84f5bRC6vh4Y4QJ9hK1QeYUpbFVa";
        when(passwordHasherMock.hash(any(PlaintextPassword.class))).thenReturn(Password.of(hashedPassword));
    }

    @Test
    void shouldBeginTransaction() {
        // When
        sut.handle(command);

        // Then
        verify(uowMock, times(1)).begin();
    }

    @Test
    void shouldCommitTransaction() {
        // When
        sut.handle(command);

        // Then
        verify(uowMock, times(1)).commit();
    }

    @Test
    void shouldCreateNewStaffAccount() {
        // When
        sut.handle(command);

        // Then
        ArgumentCaptor<StaffAccount> accountCaptor = ArgumentCaptor.forClass(StaffAccount.class);
        verify(staffAccountWriteRepoMock, times(1)).create(accountCaptor.capture());
        StaffAccount staffAccount = accountCaptor.getValue();
        assertThat(staffAccount.getUsername().getValue()).isEqualTo(command.username());
    }

    @Test
    void shouldReturnExpectedCommandResponse() {
        // When
        RegisterStaffAccountCommandResponse commandResponse = sut.handle(command);

        // Then
        ArgumentCaptor<StaffAccount> accountCaptor = ArgumentCaptor.forClass(StaffAccount.class);
        verify(staffAccountWriteRepoMock).create(accountCaptor.capture());
        StaffAccount registeredAccount = accountCaptor.getValue();

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.id()).isEqualTo(registeredAccount.getId().getValue().toString());
        assertThat(commandResponse.username()).isEqualTo(registeredAccount.getUsername().getValue());
        assertThat(commandResponse.tempPassword()).isNotNull();
        assertThat(commandResponse.status()).isEqualTo("PENDING_PASSWORD_CHANGE");
        assertThat(commandResponse.version()).isEqualTo(1);
    }

    @Test
    void shouldAppendTemporaryPasswordToStaffAccountPasswordHistory() {
        // When
        sut.handle(command);

        // Then
        ArgumentCaptor<PasswordHistoryEntry> passwordHistoryEntryCaptor = ArgumentCaptor.forClass(PasswordHistoryEntry.class);
        verify(staffAccountPasswordHistoryWriteRepoMock, times(1))
                .appendEntry(passwordHistoryEntryCaptor.capture());
        PasswordHistoryEntry capturedEntry = passwordHistoryEntryCaptor.getValue();
        assertThat(capturedEntry.staffAccountId()).isNotNull();
        assertThat(capturedEntry.hashedPassword()).isEqualTo(Password.of(hashedPassword));
        assertThat(capturedEntry.isTemporary()).isTrue();
        assertThat(capturedEntry.changedAt()).isNotNull();
    }

    @Test
    void shouldPublishDomainEvents() {
        // When
        sut.handle(command);

        // Then
        ArgumentCaptor<List<DomainEvent>> domainEventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventBusMock, times(1)).publishAll(domainEventsCaptor.capture());
        assertThat(domainEventsCaptor.getValue()).isNotEmpty();
    }

    @Test
    void whenRegisteringStaffAccountUsernameAlreadyExists_shouldThrowAppException() {
        // Given
        when(staffAccountWriteRepoMock.getByUsername(any(Username.class)))
                .thenReturn(Optional.of(
                        new StaffAccountFixture()
                                .withUsername(command.username())
                                .build())
                );

        AppException expectedAppException = new AppException(AppExceptionInfo.staffAccountUsernameAlreadyExists(command.username()));

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isEqualTo(expectedAppException);
    }

    @Test
    void whenDomainExceptionIsThrown_shouldRollbackAndTranslateToAppException() {
        // Given
        RegisterStaffAccountCommand command = new RegisterStaffAccountCommand(
                "", // forces a domain exception (UsernameException)
                "testuser@example.com",
                7,
                14,
                List.of(UUID.randomUUID().toString()),
                UUID.randomUUID().toString()
        );

        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class);
        verify(uowMock, times(1)).rollback();
    }

    @Test
    void whenInfraExceptionIsThrown_shouldRollbackAndTranslateToAppException() {
        // Given
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        doThrow(InfraException.class)
                .when(staffAccountWriteRepoMock)
                .create(any(StaffAccount.class));

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class);
        verify(uowMock, times(1)).rollback();
    }

    private RegisterStaffAccountCommand createValidRegisterStaffAccountCommand() {
        return new RegisterStaffAccountCommand(
                "testuser",
                "testuser@example.com",
                7,
                14,
                List.of("MANAGE_ACCOUNTS"),
                UUID.randomUUID().toString()
        );
    }
}
