package com.paragon.api;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
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
        private final String registeringStaffId;
        private final RegisterStaffAccountCommandResponse commandResponse;

        public Register() {
            registerStaffAccountCommandHandlerMock = mock(RegisterStaffAccountCommandHandler.class);
            sut = new StaffAccountController(registerStaffAccountCommandHandlerMock);

            commandResponse = new RegisterStaffAccountCommandResponse(
                    "id", "username123", "pending_password_change", 1
            );
            when(registerStaffAccountCommandHandlerMock.handle(any(RegisterStaffAccountCommand.class)))
                    .thenReturn(commandResponse);

            registeringStaffId = "staff-id";
            Jwt jwt = Jwt.withTokenValue("fake-token")
                    .header("alg", "none")
                    .claim("staff_id", registeringStaffId)
                    .build();

            Authentication auth = new UsernamePasswordAuthenticationToken(jwt, jwt.getTokenValue());
            SecurityContextHolder.getContext().setAuthentication(auth);
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
                    .isInstanceOf(AppException.class);
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
                    registeringStaffId,
                    request.username(),
                    request.email(),
                    request.tempPassword(),
                    request.orderAccessDuration(),
                    request.modmailTranscriptAccessDuration(),
                    request.permissionIds()
            );
        }
    }
}
