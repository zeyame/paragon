package com.paragon.application.commands;

import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommand;
import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommandHandler;
import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.events.EventBus;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class ResetStaffAccountPasswordCommandHandlerTests {

    private final ResetStaffAccountPasswordCommandHandler sut;
    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private final UnitOfWork uowMock;
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final PasswordHasher passwordHasherMock;
    private final ResetStaffAccountPasswordCommand command;
    private final StaffAccount staffAccount;

    public ResetStaffAccountPasswordCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        uowMock = mock(UnitOfWork.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        passwordHasherMock = mock(PasswordHasher.class);

        sut = new ResetStaffAccountPasswordCommandHandler(
                staffAccountWriteRepoMock,
                uowMock,
                eventBusMock,
                appExceptionHandlerMock,
                passwordHasherMock
        );

        command = new ResetStaffAccountPasswordCommand(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        staffAccount = new StaffAccountFixture()
                .withStatus(StaffAccountStatus.ACTIVE)
                .withPassword("old-password")
                .withPasswordTemporary(false)
                .withPasswordIssuedAt(Instant.now().minusSeconds(60))
                .build();
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.of(staffAccount));

        when(passwordHasherMock.hash(anyString()))
                .thenReturn("hashed-password");
    }

    @Test
    void shouldBeginTransaction() {
        // When
        sut.handle(command);

        // Then
        verify(uowMock, times(1)).begin();
    }

    @Test
    void shouldResetPasswordAndCommitTransaction() {
        // Given
        ArgumentCaptor<StaffAccount> staffAccountCaptor = ArgumentCaptor.forClass(StaffAccount.class);

        // When
        sut.handle(command);

        // Then
        verify(uowMock, times(1)).commit();

        verify(staffAccountWriteRepoMock).update(staffAccountCaptor.capture());
        StaffAccount updatedStaffAccount = staffAccountCaptor.getValue();

        assertThat(updatedStaffAccount.getPassword().getValue()).isNotEqualTo("old-password");
        assertThat(updatedStaffAccount.getPassword()).isEqualTo(Password.of("hashed-password"));
        assertThat(updatedStaffAccount.getStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
        assertThat(updatedStaffAccount.getVersion().getValue()).isEqualTo(2);
    }

    @Test
    void shouldReturnExpectedCommandResponse() {
        // When
        ResetStaffAccountPasswordCommandResponse response = sut.handle(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(staffAccount.getId().getValue().toString());
        assertThat(response.temporaryPassword()).isNotNull().hasSize(12);
        assertThat(response.status()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE.toString());
        assertThat(response.version()).isEqualTo(2);
    }

    @Test
    void shouldCallRepoGetByIdWithCorrectArgument() {
        // Given
        ArgumentCaptor<StaffAccountId> captor = ArgumentCaptor.forClass(StaffAccountId.class);

        // When
        sut.handle(command);

        // Then
        verify(staffAccountWriteRepoMock).getById(captor.capture());
        StaffAccountId staffAccountId = captor.getValue();
        assertThat(staffAccountId).isEqualTo(StaffAccountId.from(command.staffAccountIdToReset()));
    }

    @Test
    void shouldPublishDomainEvents() {
        // When
        sut.handle(command);

        // Then
        verify(eventBusMock, times(1)).publishAll(anyList());
    }

    @Test
    void givenStaffAccountDoesNotExist_shouldThrowAppException() {
        // Given
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.empty());

        String expectedErrorMessage = AppExceptionInfo.staffAccountNotFound(command.staffAccountIdToReset()).getMessage();
        int expectedErrorCode = AppExceptionInfo.staffAccountNotFound(command.staffAccountIdToReset()).getAppErrorCode();

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class)
                .extracting("message", "errorCode")
                .containsExactly(expectedErrorMessage, expectedErrorCode);
    }

    @Test
    void givenDomainExceptionThrown_shouldRollbackAndTranslateToAppException() {
        // Given
        ResetStaffAccountPasswordCommand invalidCommand = new ResetStaffAccountPasswordCommand(
                "",
                command.requestingStaffAccountId()
        );
        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatThrownBy(() -> sut.handle(invalidCommand))
                .isInstanceOf(AppException.class);
        verify(uowMock, times(1)).rollback();
    }

    @Test
    void givenInfraExceptionThrown_shouldRollbackAndTranslateToAppException() {
        // Given
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        doThrow(InfraException.class)
                .when(staffAccountWriteRepoMock)
                .update(any(StaffAccount.class));

        // When & Then
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(AppException.class);
        verify(uowMock, times(1)).rollback();
    }
}
