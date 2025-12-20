package com.paragon.application.commands;

import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommand;
import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommandHandler;
import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.events.EventBus;
import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repositories.StaffAccountRequestWriteRepo;
import com.paragon.domain.interfaces.repositories.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
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

public class SubmitPasswordChangeRequestCommandHandlerTests {
    private final SubmitPasswordChangeRequestCommandHandler sut;
    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private final StaffAccountRequestWriteRepo staffAccountRequestWriteRepoMock;
    private final EventBus eventBusMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final SubmitPasswordChangeRequestCommand command;
    private final StaffAccount staffAccount;

    public SubmitPasswordChangeRequestCommandHandlerTests() {
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        staffAccountRequestWriteRepoMock = mock(StaffAccountRequestWriteRepo.class);
        eventBusMock = mock(EventBus.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);

        sut = new SubmitPasswordChangeRequestCommandHandler(
                staffAccountWriteRepoMock,
                staffAccountRequestWriteRepoMock,
                eventBusMock,
                appExceptionHandlerMock
        );

        command = new SubmitPasswordChangeRequestCommand(
                UUID.randomUUID().toString()
        );

        staffAccount = new StaffAccountFixture()
                .withId(command.staffAccountId())
                .withCreatedBy(UUID.randomUUID().toString())
                .build();

        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.of(staffAccount));

        when(staffAccountRequestWriteRepoMock.existsPendingRequestBySubmitterAndType(
                any(StaffAccountId.class),
                any(StaffAccountRequestType.class)))
                .thenReturn(false);
    }

    @Test
    void shouldSubmitPasswordChangeRequest() {
        // Given
        ArgumentCaptor<StaffAccountRequest> requestCaptor = ArgumentCaptor.forClass(StaffAccountRequest.class);

        // When
        sut.handle(command);

        // Then
        verify(staffAccountRequestWriteRepoMock, times(1)).create(requestCaptor.capture());

        StaffAccountRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getSubmittedBy()).isEqualTo(StaffAccountId.from(command.staffAccountId()));
        assertThat(capturedRequest.getRequestType()).isEqualTo(StaffAccountRequestType.PASSWORD_CHANGE);
        assertThat(capturedRequest.getTargetId()).isNull();
        assertThat(capturedRequest.getTargetType()).isNull();
        assertThat(capturedRequest.getStatus()).isEqualTo(StaffAccountRequestStatus.PENDING);
        assertThat(capturedRequest.getSubmittedAt()).isNotNull();
        assertThat(capturedRequest.getExpiresAt()).isNotNull();
        assertThat(capturedRequest.getVersion().getValue()).isEqualTo(1);
    }

    @Test
    void shouldReturnExpectedResponse() {
        // When
        SubmitPasswordChangeRequestCommandResponse response = sut.handle(command);

        // Then
        assertThat(response.requestId()).isNotNull();
        assertThat(response.submittedBy()).isEqualTo(command.staffAccountId());
        assertThat(response.requestType()).isEqualTo(StaffAccountRequestType.PASSWORD_CHANGE.toString());
        assertThat(response.status()).isEqualTo(StaffAccountRequestStatus.PENDING.toString());
        assertThat(response.submittedAtUtc()).isNotNull();
        assertThat(response.expiresAtUtc()).isNotNull();
    }

    @Test
    void shouldGetStaffAccountById_withCorrectId() {
        // Given
        ArgumentCaptor<StaffAccountId> staffAccountIdCaptor = ArgumentCaptor.forClass(StaffAccountId.class);

        // When
        sut.handle(command);

        // Then
        verify(staffAccountWriteRepoMock, times(1))
                .getById(staffAccountIdCaptor.capture());

        StaffAccountId capturedId = staffAccountIdCaptor.getValue();
        assertThat(capturedId).isEqualTo(StaffAccountId.from(command.staffAccountId()));
    }

    @Test
    void shouldCheckForPendingPasswordChangeRequest_withCorrectParameters() {
        // Given
        ArgumentCaptor<StaffAccountId> submitterCaptor = ArgumentCaptor.forClass(StaffAccountId.class);
        ArgumentCaptor<StaffAccountRequestType> requestTypeCaptor = ArgumentCaptor.forClass(StaffAccountRequestType.class);

        // When
        sut.handle(command);

        // Then
        verify(staffAccountRequestWriteRepoMock, times(1))
                .existsPendingRequestBySubmitterAndType(submitterCaptor.capture(), requestTypeCaptor.capture());

        assertThat(submitterCaptor.getValue()).isEqualTo(StaffAccountId.from(command.staffAccountId()));
        assertThat(requestTypeCaptor.getValue()).isEqualTo(StaffAccountRequestType.PASSWORD_CHANGE);
    }

    @Test
    void shouldPublishDomainEvents() {
        // When
        sut.handle(command);

        // Then
        verify(eventBusMock, times(1)).publishAll(anyList());
    }

    @Test
    void shouldThrowAppException_whenStaffAccountNotFound() {
        // Given
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command));
    }

    @Test
    void shouldThrowAppException_whenPendingPasswordChangeRequestAlreadyExists() {
        // Given
        when(staffAccountRequestWriteRepoMock.existsPendingRequestBySubmitterAndType(
                any(StaffAccountId.class),
                eq(StaffAccountRequestType.PASSWORD_CHANGE)))
                .thenReturn(true);

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command));
    }

    @Test
    void shouldPropagateAppException_whenDomainExceptionThrown() {
        // Given
        SubmitPasswordChangeRequestCommand invalidCommand = new SubmitPasswordChangeRequestCommand("");

        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(invalidCommand));
    }

    @Test
    void shouldPropagateAppException_whenInfraExceptionThrown() {
        // Given
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        doThrow(InfraException.class)
                .when(staffAccountRequestWriteRepoMock)
                .create(any(StaffAccountRequest.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command));
    }
}
