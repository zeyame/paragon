package com.paragon.api;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountRequestDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountResponseDto;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommand;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandHandler;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class AuthControllerTests {
    @Nested
    class Login {
        private final AuthController sut;
        private final LoginStaffAccountCommandHandler loginStaffAccountCommandHandlerMock;
        private final LoginStaffAccountRequestDto requestDto;
        private final LoginStaffAccountCommandResponse commandResponse;

        public Login() {
            loginStaffAccountCommandHandlerMock = mock(LoginStaffAccountCommandHandler.class);
            sut = new AuthController(loginStaffAccountCommandHandlerMock);

            requestDto = createValidLoginStaffAccountRequestDto();
            commandResponse = new LoginStaffAccountCommandResponse(
                    UUID.randomUUID().toString(),
                    requestDto.username(),
                    false,
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
            LoginStaffAccountCommand expectedCommand = createLoginStaffAccountCommandFrom(requestDto);
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
        void whenHandlerThrowsAppException_shouldPropagateException() {
            // Given
            when(loginStaffAccountCommandHandlerMock.handle(any(LoginStaffAccountCommand.class)))
                    .thenThrow(AppException.class);

            // When & Then
            assertThatThrownBy(() -> sut.login(requestDto).join())
                    .hasCauseInstanceOf(AppException.class);
        }

        private LoginStaffAccountCommand createLoginStaffAccountCommandFrom(LoginStaffAccountRequestDto requestDto) {
            return new LoginStaffAccountCommand(
                    requestDto.username(),
                    requestDto.password()
            );
        }

        private LoginStaffAccountRequestDto createValidLoginStaffAccountRequestDto() {
            return new LoginStaffAccountRequestDto(
                    "john_doe",
                    "$argon2id$v=19$m=65536,t=3,p=1$QWxhZGRpbjpPcGVuU2VzYW1l$2iYvT1yzFzHtXJH7zM4jW1Z2sK7Tg=="
            );
        }
    }
}
