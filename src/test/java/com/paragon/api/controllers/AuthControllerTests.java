package com.paragon.api.controllers;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountRequestDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountResponseDto;
import com.paragon.api.dtos.auth.refresh.RefreshStaffAccountTokenResponseDto;
import com.paragon.api.security.HttpContextHelper;
import com.paragon.api.security.JwtGenerator;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommand;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandHandler;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandResponse;
import com.paragon.application.commands.logoutstaffaccount.LogoutStaffAccountCommand;
import com.paragon.application.commands.logoutstaffaccount.LogoutStaffAccountCommandHandler;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommand;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommandHandler;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class AuthControllerTests {
    @Nested
    class Login {
        private final AuthController sut;
        private final LoginStaffAccountCommandHandler loginStaffAccountCommandHandlerMock;
        private final HttpContextHelper httpContextHelperMock;
        private final JwtGenerator jwtGeneratorMock;
        private final RefreshStaffAccountTokenCommandHandler refreshStaffAccountTokenCommandHandlerMock;
        private final LoginStaffAccountRequestDto requestDto;
        private final LoginStaffAccountCommandResponse commandResponse;
        private final LogoutStaffAccountCommandHandler logoutStaffAccountCommandHandlerMock;

        public Login() {
            loginStaffAccountCommandHandlerMock = mock(LoginStaffAccountCommandHandler.class);
            httpContextHelperMock = mock(HttpContextHelper.class);
            jwtGeneratorMock = mock(JwtGenerator.class);
            refreshStaffAccountTokenCommandHandlerMock = mock(RefreshStaffAccountTokenCommandHandler.class);
            logoutStaffAccountCommandHandlerMock = mock(LogoutStaffAccountCommandHandler.class);
            TaskExecutor taskExecutor = Runnable::run;

            when(httpContextHelperMock.extractIpAddress()).thenReturn("192.168.1.1");
            when(jwtGeneratorMock.generateAccessToken(anyString(), any(List.class))).thenReturn("generated.jwt.token");

            sut = new AuthController(
                    loginStaffAccountCommandHandlerMock, httpContextHelperMock,
                    jwtGeneratorMock, taskExecutor, refreshStaffAccountTokenCommandHandlerMock,
                    logoutStaffAccountCommandHandlerMock
            );

            requestDto = createValidLoginStaffAccountRequestDto();
            commandResponse = new LoginStaffAccountCommandResponse(
                    UUID.randomUUID().toString(),
                    requestDto.username(),
                    false,
                    "PlainRefreshToken123",
                    List.of("MANAGE_ACCOUNTS", "VIEW_ACCOUNTS_LIST"),
                    1
            );

            when(loginStaffAccountCommandHandlerMock.handle(any(LoginStaffAccountCommand.class))).thenReturn(commandResponse);
        }

        @Test
        void shouldReturnOkResponse() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<LoginStaffAccountResponseDto>>> futureDto = sut.login(requestDto);

            // Then
            ResponseEntity<ResponseDto<LoginStaffAccountResponseDto>> completedResponse = futureDto.join();
            assertThat(completedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void callsHandlerWithCorrectCommand() {
            // Given
            LoginStaffAccountCommand expectedCommand = createLoginStaffAccountCommandFrom(requestDto, "192.168.1.1");
            ArgumentCaptor<LoginStaffAccountCommand> commandCaptor = ArgumentCaptor.forClass(LoginStaffAccountCommand.class);

            // When
            sut.login(requestDto);

            // Then
            verify(loginStaffAccountCommandHandlerMock, times(1)).handle(commandCaptor.capture());
            assertThat(commandCaptor.getValue()).isEqualTo(expectedCommand);
        }

        @Test
        void returnsExpectedResponse() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<LoginStaffAccountResponseDto>>> futureDto = sut.login(requestDto);

            // Then
            ResponseDto<LoginStaffAccountResponseDto> responseDto = futureDto.join().getBody();
            assertThat(responseDto.result()).isNotNull();
            assertThat(responseDto.errorDto()).isNull();

            LoginStaffAccountResponseDto result = responseDto.result();
            assertThat(result.id()).isEqualTo(commandResponse.id());
            assertThat(result.username()).isEqualTo(commandResponse.username());
            assertThat(result.requiresPasswordReset()).isEqualTo(commandResponse.requiresPasswordReset());
            assertThat(result.version()).isEqualTo(commandResponse.version());
        }

        @Test
        void shouldGenerateJwtAndSetItInResponseHeader() {
            // When
            sut.login(requestDto).join();

            // Then
            verify(jwtGeneratorMock, times(1)).generateAccessToken(anyString(), any(List.class));
            verify(httpContextHelperMock, times(1)).setJwtHeader("generated.jwt.token");
        }

        @Test
        void shouldSetRefreshTokenInCookie() {
            // When
            sut.login(requestDto).join();

            // Then
            verify(httpContextHelperMock, times(1)).setRefreshTokenCookie(commandResponse.plainRefreshToken());
        }

        @Test
        void whenHandlerThrowsAppException_shouldPropagateException() {
            // Given
            when(loginStaffAccountCommandHandlerMock.handle(any(LoginStaffAccountCommand.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.login(requestDto).join())
                    .hasCauseInstanceOf(AppException.class);
        }

        private LoginStaffAccountCommand createLoginStaffAccountCommandFrom(LoginStaffAccountRequestDto requestDto, String ipAddress) {
            return new LoginStaffAccountCommand(
                    requestDto.username(),
                    requestDto.password(),
                    ipAddress
            );
        }

        private LoginStaffAccountRequestDto createValidLoginStaffAccountRequestDto() {
            return new LoginStaffAccountRequestDto(
                    "john_doe",
                    "John_doe123?"
            );
        }
    }

    @Nested
    class Refresh {
        private final AuthController sut;
        private final LoginStaffAccountCommandHandler loginStaffAccountCommandHandlerMock;
        private final RefreshStaffAccountTokenCommandHandler refreshStaffAccountTokenCommandHandlerMock;
        private final HttpContextHelper httpContextHelperMock;
        private final JwtGenerator jwtGeneratorMock;
        private final RefreshStaffAccountTokenCommandResponse commandResponse;
        private final LogoutStaffAccountCommandHandler logoutStaffAccountCommandHandlerMock;

        public Refresh() {
            loginStaffAccountCommandHandlerMock = mock(LoginStaffAccountCommandHandler.class);
            refreshStaffAccountTokenCommandHandlerMock = mock(RefreshStaffAccountTokenCommandHandler.class);
            httpContextHelperMock = mock(HttpContextHelper.class);
            jwtGeneratorMock = mock(JwtGenerator.class);
            logoutStaffAccountCommandHandlerMock = mock(LogoutStaffAccountCommandHandler.class);
            TaskExecutor taskExecutor = Runnable::run;

            when(httpContextHelperMock.extractRefreshTokenFromCookie()).thenReturn("plain-token");
            when(jwtGeneratorMock.generateAccessToken(anyString(), any(List.class))).thenReturn("generated.jwt.token");

            sut = new AuthController(
                    loginStaffAccountCommandHandlerMock,
                    httpContextHelperMock,
                    jwtGeneratorMock,
                    taskExecutor,
                    refreshStaffAccountTokenCommandHandlerMock,
                    logoutStaffAccountCommandHandlerMock
            );

            commandResponse = new RefreshStaffAccountTokenCommandResponse(
                    UUID.randomUUID().toString(),
                    "john_doe",
                    false,
                    "new-plain-token",
                    List.of("MANAGE_ACCOUNTS"),
                    2
            );

            when(refreshStaffAccountTokenCommandHandlerMock.handle(any(RefreshStaffAccountTokenCommand.class)))
                    .thenReturn(commandResponse);
        }

        @Test
        void shouldReturnOkResponse() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<RefreshStaffAccountTokenResponseDto>>> futureDto = sut.refresh();

            // Then
            assertThat(futureDto.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void shouldReturnExpectedResponseDto() {
            // When
            ResponseDto<RefreshStaffAccountTokenResponseDto> responseDto = sut.refresh().join().getBody();

            // Then
            assertThat(responseDto.errorDto()).isNull();

            RefreshStaffAccountTokenResponseDto result = responseDto.result();
            assertThat(result.id()).isEqualTo(commandResponse.staffAccountId());
            assertThat(result.username()).isEqualTo(commandResponse.username());
            assertThat(result.requiresPasswordReset()).isEqualTo(commandResponse.requiresPasswordReset());
            assertThat(result.version()).isEqualTo(commandResponse.version());
        }

        @Test
        void shouldPassCorrectCommandToHandler() {
            // When
            sut.refresh();

            // Then
            ArgumentCaptor<RefreshStaffAccountTokenCommand> commandCaptor = ArgumentCaptor.forClass(RefreshStaffAccountTokenCommand.class);
            verify(refreshStaffAccountTokenCommandHandlerMock).handle(commandCaptor.capture());
            assertThat(commandCaptor.getValue().plainToken()).isEqualTo("plain-token");
        }

        @Test
        void shouldSetJwt() {
            // When
            sut.refresh().join();

            // Then
            verify(jwtGeneratorMock).generateAccessToken(commandResponse.staffAccountId(), commandResponse.permissionCodes());
            verify(httpContextHelperMock).setJwtHeader("generated.jwt.token");
        }

        @Test
        void shouldSetRefreshToken() {
            // When
            sut.refresh().join();

            // Then
            verify(httpContextHelperMock).setRefreshTokenCookie(commandResponse.plainRefreshToken());
        }

        @Test
        void shouldPropagateAppException_whenHandlerThrows() {
            when(refreshStaffAccountTokenCommandHandlerMock.handle(any(RefreshStaffAccountTokenCommand.class)))
                    .thenThrow(AppException.class);

            assertThatThrownBy(() -> sut.refresh().join())
                    .hasCauseInstanceOf(AppException.class);
        }
    }

    @Nested
    class Logout {
        private final AuthController sut;
        private final LoginStaffAccountCommandHandler loginStaffAccountCommandHandlerMock;
        private final RefreshStaffAccountTokenCommandHandler refreshStaffAccountTokenCommandHandlerMock;
        private final LogoutStaffAccountCommandHandler logoutStaffAccountCommandHandlerMock;
        private final HttpContextHelper httpContextHelperMock;
        private final JwtGenerator jwtGeneratorMock;

        public Logout() {
            loginStaffAccountCommandHandlerMock = mock(LoginStaffAccountCommandHandler.class);
            refreshStaffAccountTokenCommandHandlerMock = mock(RefreshStaffAccountTokenCommandHandler.class);
            logoutStaffAccountCommandHandlerMock = mock(LogoutStaffAccountCommandHandler.class);
            httpContextHelperMock = mock(HttpContextHelper.class);
            jwtGeneratorMock = mock(JwtGenerator.class);

            when(httpContextHelperMock.extractRefreshTokenFromCookie()).thenReturn("plain-token");

            TaskExecutor taskExecutor = Runnable::run;
            sut = new AuthController(
                    loginStaffAccountCommandHandlerMock,
                    httpContextHelperMock,
                    jwtGeneratorMock,
                    taskExecutor,
                    refreshStaffAccountTokenCommandHandlerMock,
                    logoutStaffAccountCommandHandlerMock
            );
        }

        @Test
        void shouldReturnOkResponse() {
            // When
            CompletableFuture<ResponseEntity<ResponseDto<Void>>> futureDto = sut.logout();

            // Then
            assertThat(futureDto.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void shouldPassPlainTokenFromCookieToCommand() {
            // When
            sut.logout();

            // Then
            ArgumentCaptor<LogoutStaffAccountCommand> commandCaptor = ArgumentCaptor.forClass(LogoutStaffAccountCommand.class);
            verify(logoutStaffAccountCommandHandlerMock).handle(commandCaptor.capture());
            assertThat(commandCaptor.getValue().plainToken()).isEqualTo("plain-token");
        }

        @Test
        void shouldClearRefreshTokenCookie() {
            // WHen
            sut.logout().join();

            // Then
            verify(httpContextHelperMock, times(1)).clearRefreshTokenCookie();
        }

        @Test
        void shouldPropagateAppException_whenHandlerThrows() {
            // Given
            when(logoutStaffAccountCommandHandlerMock.handle(any(LogoutStaffAccountCommand.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.logout().join())
                    .hasCauseInstanceOf(AppException.class);
        }
    }
}
