package com.paragon.application.commands;

import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionHandler;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.context.ActorContext;
import com.paragon.application.events.EventBus;
import com.paragon.application.queries.repositoryinterfaces.PermissionReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountRegisteredEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.entities.Permission;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.PermissionFixture;
import com.paragon.helpers.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RegisterStaffAccountCommandHandlerTests {
    private final RegisterStaffAccountCommandHandler sut;
    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private final PermissionReadRepo permissionReadRepoMock;
    private final ActorContext actorContextMock;
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final RegisterStaffAccountCommand command;
    private final StaffAccount requestingStaffAccount;
    private final Permission manageAccountsPermission;

    public RegisterStaffAccountCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        permissionReadRepoMock = mock(PermissionReadRepo.class);
        actorContextMock = mock(ActorContext.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);

        sut = new RegisterStaffAccountCommandHandler(staffAccountWriteRepoMock, permissionReadRepoMock, actorContextMock, eventBusMock, appExceptionHandlerMock);

        command = createValidRegisterStaffAccountCommand();

        String registeringStaffAccountId = UUID.randomUUID().toString();
        when(actorContextMock.getActorId()).thenReturn(registeringStaffAccountId);

        String manageAccountsPermissionId = UUID.randomUUID().toString();
        requestingStaffAccount = new StaffAccountFixture()
                .withId(registeringStaffAccountId)
                .withPermissionIds(List.of(manageAccountsPermissionId))
                .build();
        when(staffAccountWriteRepoMock.getById(StaffAccountId.from(registeringStaffAccountId)))
                .thenReturn(Optional.of(requestingStaffAccount));

        manageAccountsPermission = new PermissionFixture()
                .withId(manageAccountsPermissionId)
                .build();
        when(permissionReadRepoMock.getByCode(SystemPermissions.MANAGE_ACCOUNTS))
                .thenReturn(Optional.of(manageAccountsPermission));
    }

    @Test
    void givenValidCommand_shouldRegisterSuccessfully() {
        // Given
        ArgumentCaptor<StaffAccount> accountCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When
        RegisterStaffAccountCommandResponse commandResponse = sut.handle(command);

        // Then
        verify(staffAccountWriteRepoMock, times(1)).create(accountCaptor.capture());
        StaffAccount registeredAccount = accountCaptor.getValue();

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.id()).isEqualTo(registeredAccount.getId().getValue().toString());
        assertThat(commandResponse.username()).isEqualTo(registeredAccount.getUsername().getValue());
        assertThat(commandResponse.status()).isEqualTo(registeredAccount.getStatus().toString());
        assertThat(commandResponse.version()).isEqualTo(registeredAccount.getVersion().getValue());
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
        assertThat(registeredEvent.getPassword().getValue()).isEqualTo(command.tempPassword());
        assertThat(registeredEvent.getOrderAccessDuration().getValueInDays()).isEqualTo(command.orderAccessDuration());
        assertThat(registeredEvent.getModmailTranscriptAccessDuration().getValueInDays()).isEqualTo(command.modmailTranscriptAccessDuration());
        assertThat(registeredEvent.getStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
        assertThat(registeredEvent.getCreatedBy()).isEqualTo(requestingStaffAccount.getId());
    }

    @Test
    void whenRegisteringStaffAccountDoesNotExist_shouldThrowAppException() {
        // Given
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.empty());

        AppException expectedAppException = new AppException(AppExceptionInfo.staffAccountNotFound(requestingStaffAccount.getId().getValue().toString()));

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isEqualTo(expectedAppException);
    }

    @Test
    void whenRegisteringStaffAccountDoesNotHaveRequiredPermissions_shouldThrowAppException() {
        // Given
        StaffAccount accountLackingPermissions = new StaffAccountFixture()
                .withId(UUID.randomUUID().toString())
                .withPermissionIds(List.of()) // no permission
                .build();
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.of(accountLackingPermissions));

        AppException expectedAppException = new AppException(AppExceptionInfo.permissionAccessDenied("registration"));

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
                List.of(UUID.randomUUID().toString())
        );
    }
}
