package com.paragon.application.commands;

import com.paragon.application.commands.enablestaffaccount.EnableStaffAccountCommand;
import com.paragon.application.commands.enablestaffaccount.EnableStaffAccountCommandHandler;
import com.paragon.application.commands.enablestaffaccount.EnableStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.StaffAccountWriteRepo;
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

public class EnableStaffAccountCommandHandlerTests {
    private final EnableStaffAccountCommandHandler sut;
    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private final UnitOfWork uowMock;
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final EnableStaffAccountCommand command;
    private final StaffAccount staffAccountToBeEnabled;

    public EnableStaffAccountCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        uowMock = mock(UnitOfWork.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);

        sut = new EnableStaffAccountCommandHandler(
                staffAccountWriteRepoMock,
                uowMock,
                eventBusMock,
                appExceptionHandlerMock
        );

        command = new EnableStaffAccountCommand(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        staffAccountToBeEnabled = new StaffAccountFixture()
                .withId(command.staffAccountIdToBeEnabled())
                .withStatus(StaffAccountStatus.DISABLED)
                .withDisabledBy(UUID.randomUUID().toString())
                .build();

        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.of(staffAccountToBeEnabled));
    }

    @Test
    void shouldBeginTransaction() {
        sut.handle(command);

        verify(uowMock, times(1)).begin();
    }

    @Test
    void shouldEnableStaffAccountAndCommitTransaction() {
        ArgumentCaptor<StaffAccount> staffAccountCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        sut.handle(command);

        verify(uowMock, times(1)).commit();
        verify(staffAccountWriteRepoMock, times(1)).update(staffAccountCaptor.capture());

        StaffAccount enabledAccount = staffAccountCaptor.getValue();
        assertThat(enabledAccount.getId()).isEqualTo(staffAccountToBeEnabled.getId());
        assertThat(enabledAccount.getStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
        assertThat(enabledAccount.getDisabledBy()).isNull();
        assertThat(enabledAccount.getEnabledBy()).isEqualTo(StaffAccountId.from(command.requestingStaffAccountId()));
        assertThat(enabledAccount.getVersion().getValue()).isEqualTo(2);
    }

    @Test
    void shouldReturnExpectedResponse() {
        EnableStaffAccountCommandResponse response = sut.handle(command);

        assertThat(response.id()).isEqualTo(staffAccountToBeEnabled.getId().getValue().toString());
        assertThat(response.status()).isEqualTo("PENDING_PASSWORD_CHANGE");
        assertThat(response.enabledBy()).isEqualTo(command.requestingStaffAccountId());
        assertThat(response.version()).isEqualTo(2);
    }

    @Test
    void shouldCallRepoGetByIdWithCorrectArgument() {
        ArgumentCaptor<StaffAccountId> staffAccountIdCaptor = ArgumentCaptor.forClass(StaffAccountId.class);

        sut.handle(command);

        verify(staffAccountWriteRepoMock, times(1)).getById(staffAccountIdCaptor.capture());
        assertThat(staffAccountIdCaptor.getValue()).isEqualTo(StaffAccountId.from(command.staffAccountIdToBeEnabled()));
    }

    @Test
    void shouldPublishDomainEvents() {
        sut.handle(command);

        verify(eventBusMock, times(1)).publishAll(anyList());
    }

    @Test
    void givenStaffAccountToBeEnabledDoesNotExist_shouldThrowAppException() {
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.empty());

        AppException expectedException = new AppException(
                AppExceptionInfo.staffAccountNotFound(command.staffAccountIdToBeEnabled())
        );

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(expectedException.getMessage(), expectedException.getErrorCode(), expectedException.getStatusCode());
    }

    @Test
    void givenDomainExceptionThrown_shouldTranslateToAppExceptionAndRollback() {
        EnableStaffAccountCommand invalidCommand = new EnableStaffAccountCommand(
                "",
                UUID.randomUUID().toString()
        );

        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(mock(AppException.class));

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(invalidCommand));

        verify(uowMock, times(1)).rollback();
    }

    @Test
    void givenInfraExceptionThrown_shouldTranslateToAppExceptionAndRollback() {
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        doThrow(InfraException.class)
                .when(staffAccountWriteRepoMock)
                .update(any(StaffAccount.class));

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command));

        verify(uowMock, times(1)).rollback();
    }
}
