package com.paragon.application.commands;

import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommand;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandHandler;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.events.EventBus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountLoggedInEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.interfaces.TokenHasher;
import com.paragon.domain.interfaces.repos.RefreshTokenWriteRepo;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class LoginStaffAccountCommandHandlerTests {
    private final LoginStaffAccountCommandHandler sut;

    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private final RefreshTokenWriteRepo refreshTokenWriteRepoMock;
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final PasswordHasher passwordHasherMock;
    private final TokenHasher tokenHasherMock;

    private final LoginStaffAccountCommand command;
    private final StaffAccount staffAccountToLogin;

    public LoginStaffAccountCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        refreshTokenWriteRepoMock = mock(RefreshTokenWriteRepo.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        passwordHasherMock = mock(PasswordHasher.class);
        tokenHasherMock = mock(TokenHasher.class);

        sut = new LoginStaffAccountCommandHandler(
                staffAccountWriteRepoMock, refreshTokenWriteRepoMock, eventBusMock,
                appExceptionHandlerMock, passwordHasherMock, tokenHasherMock
        );

        command = new LoginStaffAccountCommand(
                "john_doe",
                "PlaintextPassword123?",
                "192.168.1.1"
        );

        staffAccountToLogin = new StaffAccountFixture()
                .withUsername(command.username())
                .withPassword("$2a$10$hashedPasswordValue")
                .withLastLoginAt(Instant.now())
                .build();

        when(staffAccountWriteRepoMock.getByUsername(any(Username.class)))
                .thenReturn(Optional.of(staffAccountToLogin));

        when(passwordHasherMock.verify(anyString(), anyString()))
                .thenReturn(true);
    }

    @Test
    void givenValidCommand_shouldLoginSuccessfully() {
        // Given
        ArgumentCaptor<StaffAccount> staffAccountArgumentCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When
        sut.handle(command);

        // Then
        verify(staffAccountWriteRepoMock).update(staffAccountArgumentCaptor.capture());
        StaffAccount loggedInStaffAccount = staffAccountArgumentCaptor.getValue();

        assertThat(loggedInStaffAccount.getUsername()).isEqualTo(staffAccountToLogin.getUsername());
        assertThat(loggedInStaffAccount.getPassword()).isEqualTo(staffAccountToLogin.getPassword());
        assertThat(loggedInStaffAccount.getLastLoginAt()).isAfterOrEqualTo(staffAccountToLogin.getLastLoginAt());
    }

    @Test
    void givenValidCommand_shouldReturnExpectedResponse() {
        // Given
        ArgumentCaptor<StaffAccount> staffAccountArgumentCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When
        LoginStaffAccountCommandResponse commandResponse = sut.handle(command);

        // Then
        verify(staffAccountWriteRepoMock).update(staffAccountArgumentCaptor.capture());
        StaffAccount loggedInStaffAccount = staffAccountArgumentCaptor.getValue();

        assertThat(commandResponse.id()).isEqualTo(loggedInStaffAccount.getId().getValue().toString());
        assertThat(commandResponse.username()).isEqualTo(loggedInStaffAccount.getUsername().getValue());
        assertThat(commandResponse.requiresPasswordReset()).isEqualTo(loggedInStaffAccount.requiresPasswordReset());
        assertThat(commandResponse.version()).isEqualTo(loggedInStaffAccount.getVersion().getValue());

        // Verify permission codes are returned
        assertThat(commandResponse.permissionCodes()).isNotNull();
        assertThat(commandResponse.permissionCodes()).hasSize(loggedInStaffAccount.getPermissionCodes().size());
    }

    @Test
    void givenValidCommand_shouldCallRepoUpdateMethodOnce_withCorrectArguments() {
        // Given
        ArgumentCaptor<StaffAccount> staffAccountArgumentCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When
        LoginStaffAccountCommandResponse commandResponse = sut.handle(command);

        // Then
        verify(staffAccountWriteRepoMock, times(1)).update(staffAccountArgumentCaptor.capture());
        StaffAccount loggedInStaffAccount = staffAccountArgumentCaptor.getValue();

        assertThat(loggedInStaffAccount)
                .usingRecursiveComparison()
                .isEqualTo(staffAccountToLogin);
    }

    @Test
    void shouldPublishDomainEvents() {
        // When
        sut.handle(command);

        // Then
        verify(eventBusMock, times(1)).publishAll(anyList());
    }

    @Test
    void givenUsernameThatDoesNotExist_shouldThrowAppException() {
        // Given
        when(staffAccountWriteRepoMock.getByUsername(any(Username.class)))
                .thenReturn(Optional.empty());

        // When & ThenThen
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class);
        
        verify(staffAccountWriteRepoMock, never()).update(any(StaffAccount.class));
    }


    @Test
    void whenPasswordIsInvalid_shouldRegisterFailedLoginAttempt() {
        // Given
        when(passwordHasherMock.verify(anyString(), anyString()))
                .thenReturn(false);

        ArgumentCaptor<StaffAccount> staffAccountArgumentCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class);

        verify(staffAccountWriteRepoMock, times(1)).update(staffAccountArgumentCaptor.capture());
        StaffAccount updatedStaffAccount = staffAccountArgumentCaptor.getValue();

        // Verify failed login attempts were incremented
        assertThat(updatedStaffAccount.getFailedLoginAttempts().getValue()).isEqualTo(1);
    }

    @Test
    void whenPasswordIsInvalid_shouldCommitTransaction() {
        // Given
        when(passwordHasherMock.verify(anyString(), anyString()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class);

//        assertThat(uowMock, times(1)).commit();
    }

    @Test
    void whenPasswordIsInvalid_shouldThrowAppException() {
        // Given
        when(passwordHasherMock.verify(anyString(), anyString()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class)
                .extracting("message", "errorCode")
                .containsExactly(AppExceptionInfo.invalidLoginCredentials().getMessage(), AppExceptionInfo.invalidLoginCredentials().getAppErrorCode());
    }

    @Test
    void whenDomainExceptionIsThrown_shouldCatchAndTranslateToAppException() {
        // Given
        LoginStaffAccountCommand command = new LoginStaffAccountCommand(
                "", // forces a domain exception when constructing Username VO
                "Password123?",
                "192.168.1.1"
        );

        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(mock(AppException.class));

        // When & ThenThen
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class);
    }

    @Test
    void whenInfraExceptionIsThrown_shouldCatchAndTranslateToAppException() {
        // Given
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        doThrow(InfraException.class)
                .when(staffAccountWriteRepoMock)
                .update(any(StaffAccount.class));

        // When & ThenThen
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class);
    }
}
