package com.paragon.api.controllers;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.disable.DisableStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.enable.EnableStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.getall.StaffAccountSummaryResponseDto;
import com.paragon.api.dtos.staffaccount.getbyusername.GetStaffAccountByUsernameResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.resetpassword.ResetStaffAccountPasswordResponseDto;
import com.paragon.api.security.HttpContextHelperImpl;
import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommand;
import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommandHandler;
import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommandResponse;
import com.paragon.application.commands.enablestaffaccount.EnableStaffAccountCommand;
import com.paragon.application.commands.enablestaffaccount.EnableStaffAccountCommandHandler;
import com.paragon.application.commands.enablestaffaccount.EnableStaffAccountCommandResponse;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommand;
import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommandHandler;
import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQuery;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryHandler;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryResponse;
import com.paragon.application.queries.getallstaffaccounts.StaffAccountSummary;
import com.paragon.application.queries.getstaffaccountbyusername.GetStaffAccountByUsernameQuery;
import com.paragon.application.queries.getstaffaccountbyusername.GetStaffAccountByUsernameQueryHandler;
import com.paragon.application.queries.getstaffaccountbyusername.GetStaffAccountByUsernameQueryResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StaffAccountControllerTests {
    @Nested
    class Register {
        private final StaffAccountController sut;
        private final RegisterStaffAccountCommandHandler registerStaffAccountCommandHandlerMock;
        private final DisableStaffAccountCommandHandler disableStaffAccountCommandHandlerMock;
        private final EnableStaffAccountCommandHandler enableStaffAccountCommandHandlerMock;
        private final ResetStaffAccountPasswordCommandHandler resetStaffAccountPasswordCommandHandlerMock;
        private final GetAllStaffAccountsQueryHandler getAllStaffAccountsQueryHandlerMock;
        private final GetStaffAccountByUsernameQueryHandler getStaffAccountByUsernameQueryHandlerMock;
        private final HttpContextHelperImpl httpContextHelperMock;
        private final RegisterStaffAccountCommandResponse commandResponse;
        private final String requestingStaffId;

        public Register() {
            registerStaffAccountCommandHandlerMock = mock(RegisterStaffAccountCommandHandler.class);
            disableStaffAccountCommandHandlerMock = mock(DisableStaffAccountCommandHandler.class);
            enableStaffAccountCommandHandlerMock = mock(EnableStaffAccountCommandHandler.class);
            resetStaffAccountPasswordCommandHandlerMock = mock(ResetStaffAccountPasswordCommandHandler.class);
            getAllStaffAccountsQueryHandlerMock = mock(GetAllStaffAccountsQueryHandler.class);
            getStaffAccountByUsernameQueryHandlerMock = mock(GetStaffAccountByUsernameQueryHandler.class);
            httpContextHelperMock = mock(HttpContextHelperImpl.class);
            TaskExecutor taskExecutor = Runnable::run;

            requestingStaffId = UUID.randomUUID().toString();
            when(httpContextHelperMock.extractAuthenticatedStaffId()).thenReturn(requestingStaffId);

            sut = new StaffAccountController(
                    registerStaffAccountCommandHandlerMock,
                    disableStaffAccountCommandHandlerMock,
                    enableStaffAccountCommandHandlerMock,
                    resetStaffAccountPasswordCommandHandlerMock,
                    getAllStaffAccountsQueryHandlerMock,
                    getStaffAccountByUsernameQueryHandlerMock,
                    httpContextHelperMock,
                    taskExecutor
            );

            commandResponse = new RegisterStaffAccountCommandResponse(
                    "id", "username123", "temp_password", "pending_password_change", 1
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
                    14,
                    7,
                    List.of("MANAGE_ACCOUNTS")
            );
        }

        private RegisterStaffAccountCommand createRegisterStaffAccountCommandFrom(RegisterStaffAccountRequestDto request) {
            return new RegisterStaffAccountCommand(
                    request.username(),
                    request.email(),
                    request.orderAccessDuration(),
                    request.modmailTranscriptAccessDuration(),
                    request.permissionCodes(),
                    requestingStaffId
            );
        }
    }

    @Nested
    class Enable {
        private final StaffAccountController sut;
        private final RegisterStaffAccountCommandHandler registerStaffAccountCommandHandlerMock;
        private final DisableStaffAccountCommandHandler disableStaffAccountCommandHandlerMock;
        private final EnableStaffAccountCommandHandler enableStaffAccountCommandHandlerMock;
        private final ResetStaffAccountPasswordCommandHandler resetStaffAccountPasswordCommandHandlerMock;
        private final GetAllStaffAccountsQueryHandler getAllStaffAccountsQueryHandlerMock;
        private final GetStaffAccountByUsernameQueryHandler getStaffAccountByUsernameQueryHandlerMock;
        private final HttpContextHelperImpl httpContextHelperMock;
        private final String staffIdToBeEnabled;
        private final String requestingStaffId;
        private final EnableStaffAccountCommandResponse commandResponse;

        public Enable() {
            registerStaffAccountCommandHandlerMock = mock(RegisterStaffAccountCommandHandler.class);
            disableStaffAccountCommandHandlerMock = mock(DisableStaffAccountCommandHandler.class);
            enableStaffAccountCommandHandlerMock = mock(EnableStaffAccountCommandHandler.class);
            resetStaffAccountPasswordCommandHandlerMock = mock(ResetStaffAccountPasswordCommandHandler.class);
            getAllStaffAccountsQueryHandlerMock = mock(GetAllStaffAccountsQueryHandler.class);
            getStaffAccountByUsernameQueryHandlerMock = mock(GetStaffAccountByUsernameQueryHandler.class);
            httpContextHelperMock = mock(HttpContextHelperImpl.class);
            TaskExecutor taskExecutor = Runnable::run;

            sut = new StaffAccountController(
                    registerStaffAccountCommandHandlerMock,
                    disableStaffAccountCommandHandlerMock,
                    enableStaffAccountCommandHandlerMock,
                    resetStaffAccountPasswordCommandHandlerMock,
                    getAllStaffAccountsQueryHandlerMock,
                    getStaffAccountByUsernameQueryHandlerMock,
                    httpContextHelperMock,
                    taskExecutor
            );

            staffIdToBeEnabled = UUID.randomUUID().toString();
            requestingStaffId = UUID.randomUUID().toString();
            commandResponse = new EnableStaffAccountCommandResponse(
                    staffIdToBeEnabled,
                    "ACTIVE",
                    requestingStaffId,
                    3
            );

            when(httpContextHelperMock.extractAuthenticatedStaffId()).thenReturn(requestingStaffId);
            when(enableStaffAccountCommandHandlerMock.handle(any(EnableStaffAccountCommand.class)))
                    .thenReturn(commandResponse);
        }

        @Test
        void shouldReturnExpectedResponseDto() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<EnableStaffAccountResponseDto>>> futureDto = sut.enable(staffIdToBeEnabled);

            // Then
            ResponseDto<EnableStaffAccountResponseDto> responseDto = futureDto.join().getBody();
            assertThat(responseDto.result()).isNotNull();
            assertThat(responseDto.errorDto()).isNull();

            EnableStaffAccountResponseDto result = responseDto.result();
            assertThat(result.id()).isEqualTo(commandResponse.id());
            assertThat(result.status()).isEqualTo(commandResponse.status());
            assertThat(result.enabledBy()).isEqualTo(commandResponse.enabledBy());
            assertThat(result.version()).isEqualTo(commandResponse.version());
        }

        @Test
        void shouldPassCorrectCommandToHandler() {
            // Given
            ArgumentCaptor<EnableStaffAccountCommand> captor = ArgumentCaptor.forClass(EnableStaffAccountCommand.class);

            // When
            sut.enable(staffIdToBeEnabled);

            // Then
            verify(enableStaffAccountCommandHandlerMock, times(1)).handle(captor.capture());
            EnableStaffAccountCommand command = captor.getValue();
            assertThat(command.staffAccountIdToBeEnabled()).isEqualTo(staffIdToBeEnabled);
            assertThat(command.requestingStaffAccountId()).isEqualTo(requestingStaffId);
        }

        @Test
        void shouldPropagateAppException_whenHandlerThrows() {
            // Given
            when(enableStaffAccountCommandHandlerMock.handle(any(EnableStaffAccountCommand.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.enable(staffIdToBeEnabled).join())
                    .hasCauseInstanceOf(AppException.class);
        }
    }

    @Nested
    class Disable {
        private final StaffAccountController sut;
        private final RegisterStaffAccountCommandHandler registerStaffAccountCommandHandlerMock;
        private final DisableStaffAccountCommandHandler disableStaffAccountCommandHandlerMock;
        private final EnableStaffAccountCommandHandler enableStaffAccountCommandHandlerMock;
        private final ResetStaffAccountPasswordCommandHandler resetStaffAccountPasswordCommandHandlerMock;
        private final GetAllStaffAccountsQueryHandler getAllStaffAccountsQueryHandlerMock;
        private final GetStaffAccountByUsernameQueryHandler getStaffAccountByUsernameQueryHandlerMock;
        private final HttpContextHelperImpl httpContextHelperMock;
        private final String staffIdToBeDisabled;
        private final String requestingStaffId;
        private final DisableStaffAccountCommandResponse commandResponse;

        public Disable() {
            registerStaffAccountCommandHandlerMock = mock(RegisterStaffAccountCommandHandler.class);
            disableStaffAccountCommandHandlerMock = mock(DisableStaffAccountCommandHandler.class);
            enableStaffAccountCommandHandlerMock = mock(EnableStaffAccountCommandHandler.class);
            resetStaffAccountPasswordCommandHandlerMock = mock(ResetStaffAccountPasswordCommandHandler.class);
            getAllStaffAccountsQueryHandlerMock = mock(GetAllStaffAccountsQueryHandler.class);
            getStaffAccountByUsernameQueryHandlerMock = mock(GetStaffAccountByUsernameQueryHandler.class);
            httpContextHelperMock = mock(HttpContextHelperImpl.class);
            TaskExecutor taskExecutor = Runnable::run;

            sut = new StaffAccountController(
                    registerStaffAccountCommandHandlerMock,
                    disableStaffAccountCommandHandlerMock,
                    enableStaffAccountCommandHandlerMock,
                    resetStaffAccountPasswordCommandHandlerMock,
                    getAllStaffAccountsQueryHandlerMock,
                    getStaffAccountByUsernameQueryHandlerMock,
                    httpContextHelperMock,
                    taskExecutor
            );

            staffIdToBeDisabled = UUID.randomUUID().toString();
            requestingStaffId = UUID.randomUUID().toString();
            commandResponse = new DisableStaffAccountCommandResponse(
                    staffIdToBeDisabled,
                    "DISABLED",
                    requestingStaffId,
                    2
            );
            when(disableStaffAccountCommandHandlerMock.handle(any(DisableStaffAccountCommand.class)))
                    .thenReturn(commandResponse);

            when(httpContextHelperMock.extractAuthenticatedStaffId())
                    .thenReturn(requestingStaffId);
        }

        @Test
        void shouldReturnExpectedResponseDto() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<DisableStaffAccountResponseDto>>> futureDto = sut.disable(staffIdToBeDisabled);

            // Then
            ResponseDto<DisableStaffAccountResponseDto> responseDto = futureDto.join().getBody();
            assertThat(responseDto.result()).isNotNull();
            assertThat(responseDto.errorDto()).isNull();

            DisableStaffAccountResponseDto result = responseDto.result();
            assertThat(result.id()).isEqualTo(commandResponse.id());
            assertThat(result.status()).isEqualTo(commandResponse.status());
            assertThat(result.disabledBy()).isEqualTo(commandResponse.disabledBy());
            assertThat(result.version()).isEqualTo(commandResponse.version());
        }

        @Test
        void shouldPassCorrectCommandToHandler() {
            // Given
            ArgumentCaptor<DisableStaffAccountCommand> commandCaptor = ArgumentCaptor.forClass(DisableStaffAccountCommand.class);

            // When
            sut.disable(staffIdToBeDisabled);

            // Then
            verify(disableStaffAccountCommandHandlerMock, times(1))
                    .handle(commandCaptor.capture());
            DisableStaffAccountCommand command = commandCaptor.getValue();

            assertThat(command.staffAccountIdToBeDisabled()).isEqualTo(staffIdToBeDisabled);
            assertThat(command.requestingStaffAccountId()).isEqualTo(requestingStaffId);
        }

        @Test
        void shouldPropagateAppExceptionWhenHandlerThrows() {
            // Given
            when(disableStaffAccountCommandHandlerMock.handle(any(DisableStaffAccountCommand.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.disable(staffIdToBeDisabled).join())
                    .hasCauseInstanceOf(AppException.class);
        }
    }

    @Nested
    class ResetPassword {
        private final StaffAccountController sut;
        private final RegisterStaffAccountCommandHandler registerStaffAccountCommandHandlerMock;
        private final DisableStaffAccountCommandHandler disableStaffAccountCommandHandlerMock;
        private final EnableStaffAccountCommandHandler enableStaffAccountCommandHandlerMock;
        private final ResetStaffAccountPasswordCommandHandler resetStaffAccountPasswordCommandHandlerMock;
        private final GetAllStaffAccountsQueryHandler getAllStaffAccountsQueryHandlerMock;
        private final GetStaffAccountByUsernameQueryHandler getStaffAccountByUsernameQueryHandlerMock;
        private final HttpContextHelperImpl httpContextHelperMock;
        private final String targetStaffAccountId;
        private final String requestingStaffId;
        private final ResetStaffAccountPasswordCommandResponse commandResponse;

        public ResetPassword() {
            registerStaffAccountCommandHandlerMock = mock(RegisterStaffAccountCommandHandler.class);
            disableStaffAccountCommandHandlerMock = mock(DisableStaffAccountCommandHandler.class);
            enableStaffAccountCommandHandlerMock = mock(EnableStaffAccountCommandHandler.class);
            resetStaffAccountPasswordCommandHandlerMock = mock(ResetStaffAccountPasswordCommandHandler.class);
            getAllStaffAccountsQueryHandlerMock = mock(GetAllStaffAccountsQueryHandler.class);
            getStaffAccountByUsernameQueryHandlerMock = mock(GetStaffAccountByUsernameQueryHandler.class);
            httpContextHelperMock = mock(HttpContextHelperImpl.class);
            TaskExecutor taskExecutor = Runnable::run;

            sut = new StaffAccountController(
                    registerStaffAccountCommandHandlerMock,
                    disableStaffAccountCommandHandlerMock,
                    enableStaffAccountCommandHandlerMock,
                    resetStaffAccountPasswordCommandHandlerMock,
                    getAllStaffAccountsQueryHandlerMock,
                    getStaffAccountByUsernameQueryHandlerMock,
                    httpContextHelperMock,
                    taskExecutor
            );

            targetStaffAccountId = UUID.randomUUID().toString();
            requestingStaffId = UUID.randomUUID().toString();
            commandResponse = new ResetStaffAccountPasswordCommandResponse(
                    targetStaffAccountId,
                    "TempPass123!",
                    "PENDING_PASSWORD_CHANGE",
                    Instant.now(),
                    3
            );

            when(httpContextHelperMock.extractAuthenticatedStaffId()).thenReturn(requestingStaffId);
            when(resetStaffAccountPasswordCommandHandlerMock.handle(any(ResetStaffAccountPasswordCommand.class)))
                    .thenReturn(commandResponse);
        }

        @Test
        void shouldReturnExpectedResponseDto() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<ResetStaffAccountPasswordResponseDto>>> futureDto = sut.resetPassword(targetStaffAccountId);

            // Then
            ResponseDto<ResetStaffAccountPasswordResponseDto> responseDto = futureDto.join().getBody();
            assertThat(responseDto.result()).isNotNull();
            assertThat(responseDto.errorDto()).isNull();

            ResetStaffAccountPasswordResponseDto result = responseDto.result();
            assertThat(result.id()).isEqualTo(commandResponse.id());
            assertThat(result.temporaryPassword()).isEqualTo(commandResponse.temporaryPassword());
            assertThat(result.status()).isEqualTo(commandResponse.status());
            assertThat(result.passwordIssuedAt()).isEqualTo(commandResponse.passwordIssuedAt());
            assertThat(result.version()).isEqualTo(commandResponse.version());
        }

        @Test
        void shouldPassCorrectCommandToHandler() {
            // Given
            ArgumentCaptor<ResetStaffAccountPasswordCommand> commandCaptor = ArgumentCaptor.forClass(ResetStaffAccountPasswordCommand.class);

            // When
            sut.resetPassword(targetStaffAccountId);

            // Then
            verify(resetStaffAccountPasswordCommandHandlerMock).handle(commandCaptor.capture());
            ResetStaffAccountPasswordCommand command = commandCaptor.getValue();
            assertThat(command.staffAccountIdToReset()).isEqualTo(targetStaffAccountId);
            assertThat(command.requestingStaffAccountId()).isEqualTo(requestingStaffId);
        }

        @Test
        void shouldPropagateAppException_whenHandlerThrows() {
            // Given
            when(resetStaffAccountPasswordCommandHandlerMock.handle(any(ResetStaffAccountPasswordCommand.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.resetPassword(targetStaffAccountId).join())
                    .hasCauseInstanceOf(AppException.class);
        }
    }

    @Nested
    class GetAll {
        private final StaffAccountController sut;
        private final RegisterStaffAccountCommandHandler registerStaffAccountCommandHandlerMock;
        private final DisableStaffAccountCommandHandler disableStaffAccountCommandHandlerMock;
        private final EnableStaffAccountCommandHandler enableStaffAccountCommandHandlerMock;
        private final ResetStaffAccountPasswordCommandHandler resetStaffAccountPasswordCommandHandlerMock;
        private final GetAllStaffAccountsQueryHandler getAllStaffAccountsQueryHandlerMock;
        private final GetStaffAccountByUsernameQueryHandler getStaffAccountByUsernameQueryHandlerMock;
        private final HttpContextHelperImpl httpContextHelperMock;
        private final GetAllStaffAccountsQueryResponse queryResponse;

        public GetAll() {
            registerStaffAccountCommandHandlerMock = mock(RegisterStaffAccountCommandHandler.class);
            disableStaffAccountCommandHandlerMock = mock(DisableStaffAccountCommandHandler.class);
            enableStaffAccountCommandHandlerMock = mock(EnableStaffAccountCommandHandler.class);
            resetStaffAccountPasswordCommandHandlerMock = mock(ResetStaffAccountPasswordCommandHandler.class);
            getAllStaffAccountsQueryHandlerMock = mock(GetAllStaffAccountsQueryHandler.class);
            getStaffAccountByUsernameQueryHandlerMock = mock(GetStaffAccountByUsernameQueryHandler.class);
            httpContextHelperMock = mock(HttpContextHelperImpl.class);
            TaskExecutor taskExecutor = Runnable::run;

            sut = new StaffAccountController(
                    registerStaffAccountCommandHandlerMock,
                    disableStaffAccountCommandHandlerMock,
                    enableStaffAccountCommandHandlerMock,
                    resetStaffAccountPasswordCommandHandlerMock,
                    getAllStaffAccountsQueryHandlerMock,
                    getStaffAccountByUsernameQueryHandlerMock,
                    httpContextHelperMock,
                    taskExecutor
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
            CompletableFuture<ResponseEntity<ResponseDto<GetAllStaffAccountsResponseDto>>> futureDto = sut.getAll(null, null, null, null, null);

            // Then
            ResponseEntity<ResponseDto<GetAllStaffAccountsResponseDto>> completedResponse = futureDto.join();
            assertThat(completedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void shouldReturnExpectedResponseDto() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<GetAllStaffAccountsResponseDto>>> futureDto = sut.getAll(null, null, null, null, null);

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

        @ParameterizedTest
        @MethodSource("provideQueryFilters")
        void callsHandler_withCorrectQuery(String status, String enabledBy, String disabledBy, String createdBefore, String createdAfter) {
            // Given
            ArgumentCaptor<GetAllStaffAccountsQuery> queryCaptor = ArgumentCaptor.forClass(GetAllStaffAccountsQuery.class);
            GetAllStaffAccountsQuery expectedQuery = new GetAllStaffAccountsQuery(status, enabledBy, disabledBy, createdBefore, createdAfter);

            // When
            sut.getAll(status, enabledBy, disabledBy, createdBefore, createdAfter);

            // Then
            verify(getAllStaffAccountsQueryHandlerMock, times(1)).handle(queryCaptor.capture());
            assertThat(queryCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedQuery);
        }

        @Test
        void whenHandlerThrowsException_shouldPropagateException() {
            // Given
            when(getAllStaffAccountsQueryHandlerMock.handle(any(GetAllStaffAccountsQuery.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.getAll(null, null, null, null, null).join())
                    .hasCauseInstanceOf(AppException.class);
        }

        private static Stream<Arguments> provideQueryFilters() {
            return Stream.of(
                    Arguments.of(null, null, null, null, null),
                    Arguments.of("ACTIVE", null, null, null, null),
                    Arguments.of("DISABLED", UUID.randomUUID().toString(), null, null, null),
                    Arguments.of("ACTIVE", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "2025-01-01T00:00:00Z", "2024-01-01T00:00:00Z"),
                    Arguments.of(null, null, UUID.randomUUID().toString(), "2023-12-31T23:59:59Z", null)
            );
        }
    }

    @Nested
    class GetByUsername {
        private final StaffAccountController sut;
        private final RegisterStaffAccountCommandHandler registerStaffAccountCommandHandlerMock;
        private final DisableStaffAccountCommandHandler disableStaffAccountCommandHandlerMock;
        private final EnableStaffAccountCommandHandler enableStaffAccountCommandHandlerMock;
        private final ResetStaffAccountPasswordCommandHandler resetStaffAccountPasswordCommandHandlerMock;
        private final GetAllStaffAccountsQueryHandler getAllStaffAccountsQueryHandlerMock;
        private final GetStaffAccountByUsernameQueryHandler getStaffAccountByUsernameQueryHandlerMock;
        private final HttpContextHelperImpl httpContextHelperMock;

        public GetByUsername() {
            registerStaffAccountCommandHandlerMock = mock(RegisterStaffAccountCommandHandler.class);
            disableStaffAccountCommandHandlerMock = mock(DisableStaffAccountCommandHandler.class);
            enableStaffAccountCommandHandlerMock = mock(EnableStaffAccountCommandHandler.class);
            resetStaffAccountPasswordCommandHandlerMock = mock(ResetStaffAccountPasswordCommandHandler.class);
            getAllStaffAccountsQueryHandlerMock = mock(GetAllStaffAccountsQueryHandler.class);
            getStaffAccountByUsernameQueryHandlerMock = mock(GetStaffAccountByUsernameQueryHandler.class);
            httpContextHelperMock = mock(HttpContextHelperImpl.class);
            TaskExecutor taskExecutor = Runnable::run;

            sut = new StaffAccountController(
                    registerStaffAccountCommandHandlerMock,
                    disableStaffAccountCommandHandlerMock,
                    enableStaffAccountCommandHandlerMock,
                    resetStaffAccountPasswordCommandHandlerMock,
                    getAllStaffAccountsQueryHandlerMock,
                    getStaffAccountByUsernameQueryHandlerMock,
                    httpContextHelperMock,
                    taskExecutor
            );
        }

        @Test
        void shouldReturnExpectedResponse() {
            // Given
            StaffAccountSummary staffAccountSummary = new StaffAccountSummary(
                    UUID.randomUUID(),
                    "john_doe",
                    "PENDING_PASSWORD_CHANGE",
                    10,
                    20,
                    Instant.now()
            );

            GetStaffAccountByUsernameQueryResponse queryResponse = new GetStaffAccountByUsernameQueryResponse(Optional.of(staffAccountSummary));
            when(getStaffAccountByUsernameQueryHandlerMock.handle(any(GetStaffAccountByUsernameQuery.class)))
                    .thenReturn(queryResponse);

            // When
            CompletableFuture<ResponseEntity<ResponseDto<GetStaffAccountByUsernameResponseDto>>> futureDto = sut.getByUsername("john_doe");

            // Then
            ResponseDto<GetStaffAccountByUsernameResponseDto> responseDto = futureDto.join().getBody();
            assertThat(responseDto.result()).isNotNull();
            assertThat(responseDto.errorDto()).isNull();;

            GetStaffAccountByUsernameResponseDto result = responseDto.result();
            assertThat(result.id()).isEqualTo(staffAccountSummary.id());
            assertThat(result.username()).isEqualTo("john_doe");
            assertThat(result.status()).isEqualTo(staffAccountSummary.status());
            assertThat(result.orderAccessDuration()).isEqualTo(staffAccountSummary.orderAccessDuration());
            assertThat(result.modmailTranscriptAccessDuration()).isEqualTo(staffAccountSummary.modmailTranscriptAccessDuration());
            assertThat(result.createdAt()).isEqualTo(staffAccountSummary.createdAtUtc());
        }

        @Test
        void shouldPassCorrectQueryToHandler() {
            // Given
            String username = "john_doe";

            // When
            sut.getByUsername(username);

            // Then
            ArgumentCaptor<GetStaffAccountByUsernameQuery> queryCaptor = ArgumentCaptor.forClass(GetStaffAccountByUsernameQuery.class);
            verify(getStaffAccountByUsernameQueryHandlerMock, times(1)).handle(queryCaptor.capture());

            GetStaffAccountByUsernameQuery capturedQuery = queryCaptor.getValue();
            assertThat(capturedQuery.username()).isEqualTo(username);
        }

        @Test
        void whenHandlerThrowsException_shouldPropagateException() {
            // Given
            when(getStaffAccountByUsernameQueryHandlerMock.handle(any(GetStaffAccountByUsernameQuery.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.getByUsername("john_doe").join())
                    .hasCauseInstanceOf(AppException.class);
        }
    }
}
