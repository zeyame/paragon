package com.paragon.api;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.getall.StaffAccountSummaryResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.api.security.HttpContextHelper;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQuery;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryHandler;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryResponse;
import com.paragon.application.queries.getallstaffaccounts.StaffAccountSummary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StaffAccountControllerTests {
    @Nested
    class Register {
        private final StaffAccountController sut;
        private final RegisterStaffAccountCommandHandler registerStaffAccountCommandHandlerMock;
        private final GetAllStaffAccountsQueryHandler getAllStaffAccountsQueryHandlerMock;
        private final HttpContextHelper httpContextHelperMock;
        private final RegisterStaffAccountCommandResponse commandResponse;
        private final String requestingStaffId;

        public Register() {
            registerStaffAccountCommandHandlerMock = mock(RegisterStaffAccountCommandHandler.class);
            getAllStaffAccountsQueryHandlerMock = mock(GetAllStaffAccountsQueryHandler.class);
            httpContextHelperMock = mock(HttpContextHelper.class);
            TaskExecutor taskExecutor = Runnable::run;

            requestingStaffId = UUID.randomUUID().toString();
            when(httpContextHelperMock.getAuthenticatedStaffId()).thenReturn(requestingStaffId);

            sut = new StaffAccountController(
                    registerStaffAccountCommandHandlerMock, getAllStaffAccountsQueryHandlerMock,
                    httpContextHelperMock, taskExecutor
            );

            commandResponse = new RegisterStaffAccountCommandResponse(
                    "id", "username123", "pending_password_change", 1
            );
            when(registerStaffAccountCommandHandlerMock.handle(any(RegisterStaffAccountCommand.class)))
                    .thenReturn(commandResponse);
        }

        @Test
        void registeringAnAccount_returnsOk() {
            // Given
            RegisterStaffAccountRequestDto request = createValidRegisterStaffAccountRequest();

            // When
            CompletableFuture<ResponseEntity<ResponseDto<RegisterStaffAccountResponseDto>>> futureDto = sut.register(request);

            // Then
            ResponseEntity<ResponseDto<RegisterStaffAccountResponseDto>> completedResponse = futureDto.join();
            assertThat(completedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void registeringAnAccount_callsHandlerWithCorrectCommand() {
            // Given
            RegisterStaffAccountRequestDto request = createValidRegisterStaffAccountRequest();
            RegisterStaffAccountCommand expectedCommand = createRegisterStaffAccountCommandFrom(request);

            ArgumentCaptor<RegisterStaffAccountCommand> commandCaptor = ArgumentCaptor.forClass(RegisterStaffAccountCommand.class);

            // When
            sut.register(request);

            // Then
            verify(registerStaffAccountCommandHandlerMock, times(1)).handle(commandCaptor.capture());
            assertThat(commandCaptor.getValue()).isEqualTo(expectedCommand);
        }

        @Test
        void registeringAnAccount_returnsExpectedResponse() {
            // Given
            RegisterStaffAccountRequestDto request = createValidRegisterStaffAccountRequest();

            // When
            CompletableFuture<ResponseEntity<ResponseDto<RegisterStaffAccountResponseDto>>> futureDto = sut.register(request);

            // Then
            ResponseDto<RegisterStaffAccountResponseDto> responseDto = futureDto.join().getBody();
            assertThat(responseDto.result()).isNotNull();
            assertThat(responseDto.errorDto()).isNull();

            RegisterStaffAccountResponseDto result = responseDto.result();
            assertThat(result.id()).isEqualTo(commandResponse.id());
            assertThat(result.username()).isEqualTo(commandResponse.username());
            assertThat(result.status()).isEqualTo(commandResponse.status());
            assertThat(result.version()).isEqualTo(commandResponse.version());
        }

        @Test
        void whenHandlerThrowsException_shouldPropagateException() {
            // Given
            RegisterStaffAccountRequestDto request = createValidRegisterStaffAccountRequest();

            when(registerStaffAccountCommandHandlerMock.handle(any(RegisterStaffAccountCommand.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.register(request).join())
                    .hasCauseInstanceOf(AppException.class);
        }

        private RegisterStaffAccountRequestDto createValidRegisterStaffAccountRequest() {
            return new RegisterStaffAccountRequestDto(
                    "username123",
                    "testemail123@gmail.com",
                    "password123",
                    14,
                    7,
                    List.of("MANAGE_ACCOUNTS")
            );
        }

        private RegisterStaffAccountCommand createRegisterStaffAccountCommandFrom(RegisterStaffAccountRequestDto request) {
            return new RegisterStaffAccountCommand(
                    request.username(),
                    request.email(),
                    request.tempPassword(),
                    request.orderAccessDuration(),
                    request.modmailTranscriptAccessDuration(),
                    request.permissionCodes(),
                    requestingStaffId
            );
        }
    }

    @Nested
    class GetAll {
        private final StaffAccountController sut;
        private final RegisterStaffAccountCommandHandler registerStaffAccountCommandHandlerMock;
        private final GetAllStaffAccountsQueryHandler getAllStaffAccountsQueryHandlerMock;
        private final HttpContextHelper httpContextHelperMock;
        private final GetAllStaffAccountsQueryResponse queryResponse;

        public GetAll() {
            registerStaffAccountCommandHandlerMock = mock(RegisterStaffAccountCommandHandler.class);
            getAllStaffAccountsQueryHandlerMock = mock(GetAllStaffAccountsQueryHandler.class);
            httpContextHelperMock = mock(HttpContextHelper.class);
            TaskExecutor taskExecutor = Runnable::run;

            sut = new StaffAccountController(
                    registerStaffAccountCommandHandlerMock, getAllStaffAccountsQueryHandlerMock,
                    httpContextHelperMock, taskExecutor
            );

            queryResponse = new GetAllStaffAccountsQueryResponse(List.of(
                    new StaffAccountSummary(
                            UUID.randomUUID(),
                            "john_doe",
                            "active",
                            10,
                            5,
                            Instant.now()
                    )
            ));

            when(getAllStaffAccountsQueryHandlerMock.handle(any(GetAllStaffAccountsQuery.class))).thenReturn(queryResponse);
        }

        @Test
        void getAllStaffAccounts_returnsOk() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<GetAllStaffAccountsResponseDto>>> futureDto = sut.getAll();

            // Then
            ResponseEntity<ResponseDto<GetAllStaffAccountsResponseDto>> completedResponse = futureDto.join();
            assertThat(completedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void callsHandler_withCorrectQuery() {
            // Given
            ArgumentCaptor<GetAllStaffAccountsQuery> queryCaptor = ArgumentCaptor.forClass(GetAllStaffAccountsQuery.class);

            // When
            sut.getAll();

            // Then
            verify(getAllStaffAccountsQueryHandlerMock, times(1)).handle(queryCaptor.capture());
            assertThat(queryCaptor.getValue()).isInstanceOf(GetAllStaffAccountsQuery.class);
        }

        @Test
        void mapsQueryResponse_toCorrectResponseDto() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<GetAllStaffAccountsResponseDto>>> futureDto = sut.getAll();

            // Then
            ResponseDto<GetAllStaffAccountsResponseDto> responseDto = futureDto.join().getBody();
            assertThat(responseDto.result()).isNotNull();
            assertThat(responseDto.errorDto()).isNull();

            GetAllStaffAccountsResponseDto actualResponseDto = responseDto.result();
            assertThat(actualResponseDto.staffAccountSummaryResponseDtos().size())
                    .isEqualTo(queryResponse.staffAccountSummaries().size());

            StaffAccountSummaryResponseDto actualSummary = actualResponseDto.staffAccountSummaryResponseDtos().getFirst();
            StaffAccountSummary expectedSummary = queryResponse.staffAccountSummaries().getFirst();

            assertThat(actualSummary.id()).isEqualTo(expectedSummary.id());
            assertThat(actualSummary.username()).isEqualTo(expectedSummary.username());
            assertThat(actualSummary.status()).isEqualTo(expectedSummary.status());
            assertThat(actualSummary.orderAccessDuration()).isEqualTo(expectedSummary.orderAccessDuration());
            assertThat(actualSummary.modmailTranscriptAccessDuration()).isEqualTo(expectedSummary.modmailTranscriptAccessDuration());
        }

        @Test
        void whenHandlerThrowsException_shouldPropagateException() {
            // Given
            when(getAllStaffAccountsQueryHandlerMock.handle(any(GetAllStaffAccountsQuery.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.getAll().join())
                    .hasCauseInstanceOf(AppException.class);
        }
    }
}
