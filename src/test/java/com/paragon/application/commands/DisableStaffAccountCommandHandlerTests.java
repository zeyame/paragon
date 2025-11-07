package com.paragon.application.commands;

import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommand;
import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommandHandler;
import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class DisableStaffAccountCommandHandlerTests {
    private final DisableStaffAccountCommandHandler sut;
    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private final UnitOfWork uowMock;
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final DisableStaffAccountCommand command;
    private final StaffAccount staffAccountToBeDisabled;

    public DisableStaffAccountCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        uowMock = mock(UnitOfWork.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        sut = new DisableStaffAccountCommandHandler(
                staffAccountWriteRepoMock, uowMock,
                eventBusMock, appExceptionHandlerMock
        );

        command = new DisableStaffAccountCommand(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        staffAccountToBeDisabled = new StaffAccountFixture()
                .withId(command.staffAccountIdToBeDisabled())
                .withCreatedBy(command.requestingStaffAccountId())
                .build();

        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.of(staffAccountToBeDisabled));

    }

    @Test
    void shouldBeginTransaction() {
        // When
        sut.handle(command);

        // Then
        verify(uowMock, times(1)).begin();
    }

    @Test
    void shouldDisableStaffAccountAndCommitTransaction() {
        // Given
        ArgumentCaptor<StaffAccount> staffAccountCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When
        sut.handle(command);

        // Then
        verify(uowMock, times(1)).commit();

        verify(staffAccountWriteRepoMock, times(1))
                .update(staffAccountCaptor.capture());
        StaffAccount disabledStaffAccount = staffAccountCaptor.getValue();
        assertThat(disabledStaffAccount.getId()).isEqualTo(staffAccountToBeDisabled.getId());
        assertThat(disabledStaffAccount.getStatus()).isEqualTo(StaffAccountStatus.DISABLED);
        assertThat(disabledStaffAccount.getDisabledBy()).isEqualTo(StaffAccountId.from(command.requestingStaffAccountId()));
        assertThat(disabledStaffAccount.getVersion().getValue()).isEqualTo(2);
    }

    @Test
    void shouldReturnExpectedResponse() {
        // When
        DisableStaffAccountCommandResponse commandResponse = sut.handle(command);

        // Then
        assertThat(commandResponse.id()).isEqualTo(staffAccountToBeDisabled.getId().getValue().toString());
        assertThat(commandResponse.status()).isEqualTo("DISABLED");
        assertThat(commandResponse.disabledBy()).isEqualTo(command.requestingStaffAccountId());
        assertThat(commandResponse.version()).isEqualTo(2);
    }

    @Test
    void shouldCallRepoGetByIdMethodOnce_WithCorrectArgument() {
        // Given
        ArgumentCaptor<StaffAccountId> staffAccountIdCaptor = ArgumentCaptor.forClass(StaffAccountId.class);

        // When
        sut.handle(command);

        // Then
        verify(staffAccountWriteRepoMock, times(1))
                .getById(staffAccountIdCaptor.capture());

        StaffAccountId staffAccountId = staffAccountIdCaptor.getValue();
        assertThat(staffAccountId).isEqualTo(StaffAccountId.from(command.staffAccountIdToBeDisabled()));
    }

    @Test
    void shouldPublishDomainEvents() {
        // When
        sut.handle(command);

        // Then
        verify(eventBusMock, times(1)).publishAll(anyList());
    }

    @Test
    void givenStaffAccountToBeDisabledDoesNotExist_shouldThrowAppException() {
        // Given
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.empty());

        AppException appException = new AppException(
                AppExceptionInfo.staffAccountNotFound(
                        command.staffAccountIdToBeDisabled()
                )
        );

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(appException.getMessage(), appException.getErrorCode(), appException.getStatusCode());
    }

    @Test
    void givenDomainExceptionThrown_shouldTranslateToAppExceptionAndRollbackTransaction() {
        // Given
        DisableStaffAccountCommand invalidCommand = new DisableStaffAccountCommand(
                "", // forces a domain exception
                UUID.randomUUID().toString()
        );

        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(invalidCommand));
        verify(uowMock, times(1)).rollback();
    }

    @Test
    void givenInfraExceptionThrown_shouldTranslateToAppExceptionAndRollbackTransaction() {
        // Given
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        doThrow(InfraException.class)
                .when(staffAccountWriteRepoMock)
                .update(any(StaffAccount.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command));
        verify(uowMock, times(1)).rollback();
    }
}
