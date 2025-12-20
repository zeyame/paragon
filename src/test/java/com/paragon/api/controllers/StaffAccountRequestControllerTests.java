package com.paragon.api.controllers;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccountrequest.submitpasswordchangerequest.SubmitPasswordChangeRequestResponseDto;
import com.paragon.api.security.HttpContextHelperImpl;
import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommand;
import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommandHandler;
import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class StaffAccountRequestControllerTests {
    @Nested
    class SubmitPasswordChangeRequest {
        private final StaffAccountRequestController sut;
        private final SubmitPasswordChangeRequestCommandHandler commandHandlerMock;
        private final HttpContextHelperImpl httpContextHelperMock;
        private final String requestingStaffId;
        private final SubmitPasswordChangeRequestCommandResponse commandResponse;

        public SubmitPasswordChangeRequest() {
            commandHandlerMock = mock(SubmitPasswordChangeRequestCommandHandler.class);
            httpContextHelperMock = mock(HttpContextHelperImpl.class);
            TaskExecutor taskExecutor = Runnable::run;

            requestingStaffId = UUID.randomUUID().toString();
            when(httpContextHelperMock.extractAuthenticatedStaffId()).thenReturn(requestingStaffId);

            sut = new StaffAccountRequestController(
                    commandHandlerMock,
                    httpContextHelperMock,
                    taskExecutor
            );

            commandResponse = new SubmitPasswordChangeRequestCommandResponse(
                    UUID.randomUUID().toString(),
                    requestingStaffId,
                    StaffAccountRequestType.PASSWORD_CHANGE.toString(),
                    StaffAccountRequestStatus.PENDING.toString(),
                    Instant.now().toString(),
                    Instant.now().plusSeconds(604800).toString()
            );
            when(commandHandlerMock.handle(any())).thenReturn(commandResponse);
        }

        @Test
        void shouldReturnOk() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<SubmitPasswordChangeRequestResponseDto>>> futureDto =
                    sut.submitPasswordChangeRequest();

            // Then
            ResponseEntity<ResponseDto<SubmitPasswordChangeRequestResponseDto>> completedResponse = futureDto.join();
            assertThat(completedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void shouldCallHandlerWithCorrectCommand() {
            // Given
            ArgumentCaptor<SubmitPasswordChangeRequestCommand> commandCaptor =
                    ArgumentCaptor.forClass(SubmitPasswordChangeRequestCommand.class);

            // When
            sut.submitPasswordChangeRequest();

            // Then
            verify(commandHandlerMock, times(1)).handle(commandCaptor.capture());
            SubmitPasswordChangeRequestCommand capturedCommand = commandCaptor.getValue();
            assertThat(capturedCommand.staffAccountId()).isEqualTo(requestingStaffId);
        }

        @Test
        void shouldReturnExpectedResponseDto() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<SubmitPasswordChangeRequestResponseDto>>> futureDto =
                    sut.submitPasswordChangeRequest();

            // Then
            ResponseDto<SubmitPasswordChangeRequestResponseDto> responseDto = futureDto.join().getBody();
            assertThat(responseDto.result()).isNotNull();
            assertThat(responseDto.errorDto()).isNull();

            SubmitPasswordChangeRequestResponseDto result = responseDto.result();
            assertThat(result.requestId()).isEqualTo(commandResponse.requestId());
            assertThat(result.submittedBy()).isEqualTo(commandResponse.submittedBy());
            assertThat(result.requestType()).isEqualTo(commandResponse.requestType());
            assertThat(result.status()).isEqualTo(commandResponse.status());
            assertThat(result.submittedAtUtc()).isEqualTo(commandResponse.submittedAtUtc());
            assertThat(result.expiresAtUtc()).isEqualTo(commandResponse.expiresAtUtc());
        }

        @Test
        void shouldPropagateAppException_whenHandlerThrows() {
            // Given
            when(commandHandlerMock.handle(any(SubmitPasswordChangeRequestCommand.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.submitPasswordChangeRequest().join())
                    .hasCauseInstanceOf(AppException.class);
        }
    }
}
