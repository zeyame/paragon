package com.paragon.application.commands;

import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.ActorContext;
import com.paragon.application.events.EventBus;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountRegisteredEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
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
    private final ActorContext actorContextMock;
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final PasswordHasher passwordHasherMock;
    private final RegisterStaffAccountCommand command;
    private final StaffAccount requestingStaffAccount;
    private final String hashedPassword;

    public RegisterStaffAccountCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        actorContextMock = mock(ActorContext.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        passwordHasherMock = mock(PasswordHasher.class);

        sut = new RegisterStaffAccountCommandHandler(staffAccountWriteRepoMock, actorContextMock, eventBusMock, appExceptionHandlerMock, passwordHasherMock);

        command = createValidRegisterStaffAccountCommand();

        String requestingStaffAccountId = UUID.randomUUID().toString();
        when(actorContextMock.getActorId()).thenReturn(requestingStaffAccountId);

        requestingStaffAccount = new StaffAccountFixture()
                .withId(requestingStaffAccountId)
                .withPermissionCodes(List.of(SystemPermissions.MANAGE_ACCOUNTS.getValue()))
                .build();
        when(staffAccountWriteRepoMock.getById(StaffAccountId.from(requestingStaffAccountId)))
                .thenReturn(Optional.of(requestingStaffAccount));

        hashedPassword = "$2a$10$7eqJtq98hPqEX7fNZaFWoOaYp84f5bRC6vh4Y4QJ9hK1QeYUpbFVa";
        when(passwordHasherMock.hash(anyString())).thenReturn(hashedPassword);
    }

    @Test
    void givenValidCommand_shouldRegisterSuccessfully() {
        // Given
        ArgumentCaptor<StaffAccount> accountCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When
        RegisterStaffAccountCommandResponse commandResponse = sut.handle(command);

        // Then
        verify(staffAccountWriteRepoMock).create(accountCaptor.capture());
        StaffAccount registeredAccount = accountCaptor.getValue();

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.id()).isEqualTo(registeredAccount.getId().getValue().toString());
        assertThat(commandResponse.username()).isEqualTo(registeredAccount.getUsername().getValue());
        assertThat(commandResponse.status()).isEqualTo(registeredAccount.getStatus().toString());
        assertThat(commandResponse.version()).isEqualTo(registeredAccount.getVersion().getValue());
    }

    @Test
    void givenValidCommand_shouldPassCorrectArgumentsToRepo() {
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
    void givenValidCommand_shouldPublishStaffAccountRegisteredEvent() {
        // Given
        ArgumentCaptor<List<DomainEvent>> domainEventsCaptor =  ArgumentCaptor.forClass(List.class);

        // When
        sut.handle(command);

        // Then
        verify(eventBusMock, times(1)).publishAll(domainEventsCaptor.capture());

        List<DomainEvent> publishedEvents = domainEventsCaptor.getValue();
        assertThat(publishedEvents.size()).isEqualTo(1);

        DomainEvent event = publishedEvents.getFirst();
        assertThat(event).isInstanceOf(StaffAccountRegisteredEvent.class);

        StaffAccountRegisteredEvent registeredEvent = (StaffAccountRegisteredEvent) event;
        assertThat(registeredEvent.getUsername().getValue()).isEqualTo(command.username());
        assertThat(registeredEvent.getPassword().getValue()).isEqualTo(hashedPassword);
        assertThat(registeredEvent.getOrderAccessDuration().getValueInDays()).isEqualTo(command.orderAccessDuration());
        assertThat(registeredEvent.getModmailTranscriptAccessDuration().getValueInDays()).isEqualTo(command.modmailTranscriptAccessDuration());
        assertThat(registeredEvent.getStaffAccountStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
        assertThat(registeredEvent.getStaffAccountCreatedBy()).isEqualTo(requestingStaffAccount.getId());
    }

    @Test
    void whenRequestingStaffAccountDoesNotExist_shouldThrowAppException() {
        // Given
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.empty());

        AppException expectedAppException = new AppException(AppExceptionInfo.staffAccountNotFound(requestingStaffAccount.getId().getValue().toString()));

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isEqualTo(expectedAppException);
    }

    @Test
    void whenRequestingStaffAccountDoesNotHaveRequiredPermissions_shouldThrowAppException() {
        // Given
        StaffAccount accountLackingPermissions = new StaffAccountFixture()
                .withId(UUID.randomUUID().toString())
                .withPermissionCodes(List.of()) // no permission
                .build();
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.of(accountLackingPermissions));

        AppException expectedAppException = new AppException(AppExceptionInfo.permissionAccessDenied("registration"));

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isEqualTo(expectedAppException);
    }

    @Test
    void whenRegisteringStaffAccountUsernameAlreadyExists_shouldThrowAppException() {
        // Given
        when(staffAccountWriteRepoMock.getByUsername(any(Username.class))).thenReturn(Optional.of((StaffAccountFixture.validStaffAccount())));

        AppException expectedAppException = new AppException(AppExceptionInfo.staffAccountUsernameAlreadyExists(command.username()));

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isEqualTo(expectedAppException);
    }

    @Test
    void whenDomainExceptionIsThrown_shouldCatchAndTranslateToAppException() {
        // Given
        RegisterStaffAccountCommand command = new RegisterStaffAccountCommand(
                "", // forces a domain exception (UsernameException)
                "testuser@example.com",
                "TempPass123!",
                7,
                14,
                List.of(UUID.randomUUID().toString())
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
                .create(any(StaffAccount.class));

        // When & ThenThen
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class);
    }

    private RegisterStaffAccountCommand createValidRegisterStaffAccountCommand() {
        return new RegisterStaffAccountCommand(
                "testuser",
                "testuser@example.com",
                "TempPass123!",
                7,
                14,
                List.of("MANAGE_ACCOUNTS")
        );
    }
}
