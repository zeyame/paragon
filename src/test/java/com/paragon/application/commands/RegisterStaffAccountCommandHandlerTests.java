package com.paragon.application.commands;

import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RegisterStaffAccountCommandHandlerTests {
    private final RegisterStaffAccountCommandHandler sut;
    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private final UnitOfWork uowMock;
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final PasswordHasher passwordHasherMock;
    private final RegisterStaffAccountCommand command;
    private final String hashedPassword;

    public RegisterStaffAccountCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        uowMock = mock(UnitOfWork.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        passwordHasherMock = mock(PasswordHasher.class);

        sut = new RegisterStaffAccountCommandHandler(
                staffAccountWriteRepoMock, uowMock, eventBusMock,
                appExceptionHandlerMock, passwordHasherMock
        );

        command = createValidRegisterStaffAccountCommand();

        hashedPassword = "$2a$10$7eqJtq98hPqEX7fNZaFWoOaYp84f5bRC6vh4Y4QJ9hK1QeYUpbFVa";
        when(passwordHasherMock.hash(anyString())).thenReturn(hashedPassword);
    }

    @Test
    void shouldBeginTransaction() {
        // When
        sut.handle(command);

        // Then
        verify(uowMock, times(1)).begin();
    }

    @Test
    void shouldRegisterSuccessfully() {
        // Given
        ArgumentCaptor<StaffAccount> accountCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When
        RegisterStaffAccountCommandResponse commandResponse = sut.handle(command);

        // Then
        verify(uowMock, times(1)).commit();

        verify(staffAccountWriteRepoMock).create(accountCaptor.capture());
        StaffAccount registeredAccount = accountCaptor.getValue();

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.id()).isEqualTo(registeredAccount.getId().getValue().toString());
        assertThat(commandResponse.username()).isEqualTo(registeredAccount.getUsername().getValue());
        assertThat(commandResponse.tempPassword()).isNotNull();
        assertThat(commandResponse.status()).isEqualTo(registeredAccount.getStatus().toString());
        assertThat(commandResponse.version()).isEqualTo(registeredAccount.getVersion().getValue());
    }

    @Test
    void shouldPassCorrectArgumentsToRepoCreateMethod() {
        // Given
        ArgumentCaptor<StaffAccount> accountCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When
        sut.handle(command);

        // Then
        verify(staffAccountWriteRepoMock, times(1)).create(accountCaptor.capture());
        StaffAccount staffAccount = accountCaptor.getValue();

        assertThat(staffAccount.getUsername().getValue()).isEqualTo(command.username());
        assertThat(staffAccount.getEmail().getValue()).isEqualTo(command.email());
        assertThat(staffAccount.getPassword().getValue()).isEqualTo(hashedPassword);
        assertThat(staffAccount.getOrderAccessDuration().getValueInDays()).isEqualTo(command.orderAccessDuration());
        assertThat(staffAccount.getModmailTranscriptAccessDuration().getValueInDays()).isEqualTo(command.modmailTranscriptAccessDuration());
        assertThat(staffAccount.getPermissionCodes().stream().map(PermissionCode::getValue).toList()).isEqualTo(command.permissionCodes());
    }

    @Test
    void shouldPublishDomainEvents() {
        // When
        sut.handle(command);

        // Then
        verify(eventBusMock, times(1)).publishAll(anyList());
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
