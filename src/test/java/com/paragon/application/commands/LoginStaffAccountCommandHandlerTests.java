package com.paragon.application.commands;

import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommand;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandHandler;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionHandler;
import com.paragon.application.events.EventBus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.PasswordHasher;
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
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final PasswordHasher passwordHasherMock;
    private final LoginStaffAccountCommand command;
    private final StaffAccount staffAccountToLogin;

    public LoginStaffAccountCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        passwordHasherMock = mock(PasswordHasher.class);
        sut = new LoginStaffAccountCommandHandler(staffAccountWriteRepoMock, eventBusMock, appExceptionHandlerMock, passwordHasherMock);

        command = new LoginStaffAccountCommand(
                "john_doe",
                "PlaintextPassword123"
        );

        staffAccountToLogin = new StaffAccountFixture()
                .withUsername(command.username())
                .withPassword("$2a$10$hashedPasswordValue")
                .withLastLoginAt(Instant.now())
                .build();

        when(staffAccountWriteRepoMock.getByUsername(any(Username.class)))
                .thenReturn(Optional.of(staffAccountToLogin));

        // Mock successful password verification by default
        when(passwordHasherMock.verify(eq(command.password()), any(String.class)))
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

    // TODO: Enhance this test later
    @Test
    void givenValidCommand_shouldPublishStaffAccountLoggedInEvent() {
        // When
        sut.handle(command);

        // Then
        verify(eventBusMock, times(1)).publishAll(any(List.class));
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
    void whenDomainExceptionIsThrown_shouldCatchAndTranslateToAppException() {
        // Given
        LoginStaffAccountCommand command = new LoginStaffAccountCommand(
                "john_doe",
                "" // force a domain exception
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
