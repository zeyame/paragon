package com.paragon.api.mappers;

import com.paragon.api.dtos.staffaccount.disable.DisableStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.enable.EnableStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.getall.StaffAccountSummaryResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.resetpassword.ResetStaffAccountPasswordResponseDto;
import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommand;
import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommandResponse;
import com.paragon.application.commands.enablestaffaccount.EnableStaffAccountCommand;
import com.paragon.application.commands.enablestaffaccount.EnableStaffAccountCommandResponse;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommand;
import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommandResponse;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryResponse;
import com.paragon.application.queries.getallstaffaccounts.StaffAccountSummary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountMapperTests {
    private final StaffAccountMapper sut = new StaffAccountMapper();

    @Nested
    class ToRegisterCommand {
        @Test
        void shouldMapAllFieldsCorrectly() {
            // Given
            RegisterStaffAccountRequestDto requestDto = new RegisterStaffAccountRequestDto(
                    "john_doe",
                    "john@example.com",
                    14,
                    7,
                    List.of("MANAGE_ACCOUNTS", "VIEW_ACCOUNTS_LIST")
            );
            String requestingStaffAccountId = UUID.randomUUID().toString();

            // When
            RegisterStaffAccountCommand command = sut.toRegisterCommand(requestDto, requestingStaffAccountId);

            // Then
            assertThat(command.username()).isEqualTo(requestDto.username());
            assertThat(command.email()).isEqualTo(requestDto.email());
            assertThat(command.orderAccessDuration()).isEqualTo(requestDto.orderAccessDuration());
            assertThat(command.modmailTranscriptAccessDuration()).isEqualTo(requestDto.modmailTranscriptAccessDuration());
            assertThat(command.permissionCodes()).isEqualTo(requestDto.permissionCodes());
            assertThat(command.createdBy()).isEqualTo(requestingStaffAccountId);
        }
    }

    @Nested
    class ToRegisterResponseDto {
        @Test
        void shouldMapAllFieldsCorrectly() {
            // Given
            RegisterStaffAccountCommandResponse commandResponse = new RegisterStaffAccountCommandResponse(
                    "staff-id-123",
                    "john_doe",
                    "TempPass123!",
                    "PENDING_PASSWORD_CHANGE",
                    1
            );

            // When
            RegisterStaffAccountResponseDto responseDto = sut.toRegisterResponseDto(commandResponse);

            // Then
            assertThat(responseDto.id()).isEqualTo(commandResponse.id());
            assertThat(responseDto.username()).isEqualTo(commandResponse.username());
            assertThat(responseDto.tempPassword()).isEqualTo(commandResponse.tempPassword());
            assertThat(responseDto.status()).isEqualTo(commandResponse.status());
            assertThat(responseDto.version()).isEqualTo(commandResponse.version());
        }
    }

    @Nested
    class ToDisableCommand {
        @Test
        void shouldMapAllFieldsCorrectly() {
            // Given
            String staffAccountIdToBeDisabled = UUID.randomUUID().toString();
            String requestingStaffAccountId = UUID.randomUUID().toString();

            // When
            DisableStaffAccountCommand command = sut.toDisableCommand(staffAccountIdToBeDisabled, requestingStaffAccountId);

            // Then
            assertThat(command.staffAccountIdToBeDisabled()).isEqualTo(staffAccountIdToBeDisabled);
            assertThat(command.requestingStaffAccountId()).isEqualTo(requestingStaffAccountId);
        }
    }

    @Nested
    class ToDisableResponseDto {
        @Test
        void shouldMapAllFieldsCorrectly() {
            // Given
            DisableStaffAccountCommandResponse commandResponse = new DisableStaffAccountCommandResponse(
                    "staff-id-456",
                    "DISABLED",
                    "admin-id-789",
                    2
            );

            // When
            DisableStaffAccountResponseDto responseDto = sut.toDisableResponseDto(commandResponse);

            // Then
            assertThat(responseDto.id()).isEqualTo(commandResponse.id());
            assertThat(responseDto.status()).isEqualTo(commandResponse.status());
            assertThat(responseDto.disabledBy()).isEqualTo(commandResponse.disabledBy());
            assertThat(responseDto.version()).isEqualTo(commandResponse.version());
        }
    }

    @Nested
    class ToEnableCommand {
        @Test
        void shouldMapAllFieldsCorrectly() {
            String staffAccountIdToBeEnabled = UUID.randomUUID().toString();
            String requestingStaffAccountId = UUID.randomUUID().toString();

            EnableStaffAccountCommand command = sut.toEnableCommand(staffAccountIdToBeEnabled, requestingStaffAccountId);

            assertThat(command.staffAccountIdToBeEnabled()).isEqualTo(staffAccountIdToBeEnabled);
            assertThat(command.requestingStaffAccountId()).isEqualTo(requestingStaffAccountId);
        }
    }

    @Nested
    class ToEnableResponseDto {
        @Test
        void shouldMapAllFieldsCorrectly() {
            EnableStaffAccountCommandResponse commandResponse = new EnableStaffAccountCommandResponse(
                    "staff-id-321",
                    "ACTIVE",
                    "admin-id-999",
                    5
            );

            EnableStaffAccountResponseDto responseDto = sut.toEnableResponseDto(commandResponse);

            assertThat(responseDto.id()).isEqualTo(commandResponse.id());
            assertThat(responseDto.status()).isEqualTo(commandResponse.status());
            assertThat(responseDto.enabledBy()).isEqualTo(commandResponse.enabledBy());
            assertThat(responseDto.version()).isEqualTo(commandResponse.version());
        }
    }

    @Nested
    class ToResetPasswordCommand {
        @Test
        void shouldMapAllFieldsCorrectly() {
            // Given
            String staffAccountIdToReset = UUID.randomUUID().toString();
            String requestingStaffAccountId = UUID.randomUUID().toString();

            // When
            ResetStaffAccountPasswordCommand command = sut.toResetPasswordCommand(staffAccountIdToReset, requestingStaffAccountId);

            // Then
            assertThat(command.staffAccountIdToReset()).isEqualTo(staffAccountIdToReset);
            assertThat(command.requestingStaffAccountId()).isEqualTo(requestingStaffAccountId);
        }
    }

    @Nested
    class ToResetPasswordResponseDto {
        @Test
        void shouldMapAllFieldsCorrectly() {
            // Given
            Instant passwordIssuedAt = Instant.now();
            ResetStaffAccountPasswordCommandResponse commandResponse = new ResetStaffAccountPasswordCommandResponse(
                    "staff-id-999",
                    "NewTempPass456!",
                    "PENDING_PASSWORD_CHANGE",
                    passwordIssuedAt,
                    3
            );

            // When
            ResetStaffAccountPasswordResponseDto responseDto = sut.toResetPasswordResponseDto(commandResponse);

            // Then
            assertThat(responseDto.id()).isEqualTo(commandResponse.id());
            assertThat(responseDto.temporaryPassword()).isEqualTo(commandResponse.temporaryPassword());
            assertThat(responseDto.status()).isEqualTo(commandResponse.status());
            assertThat(responseDto.passwordIssuedAt()).isEqualTo(commandResponse.passwordIssuedAt());
            assertThat(responseDto.version()).isEqualTo(commandResponse.version());
        }
    }

    @Nested
    class ToGetAllResponseDto {
        @Test
        void shouldMapAllFieldsCorrectly() {
            // Given
            Instant createdAt = Instant.now();
            StaffAccountSummary summary1 = new StaffAccountSummary(
                    UUID.randomUUID(),
                    "john_doe",
                    "ACTIVE",
                    14,
                    7,
                    createdAt
            );
            StaffAccountSummary summary2 = new StaffAccountSummary(
                    UUID.randomUUID(),
                    "jane_smith",
                    "DISABLED",
                    30,
                    14,
                    createdAt.plusSeconds(3600)
            );
            GetAllStaffAccountsQueryResponse queryResponse = new GetAllStaffAccountsQueryResponse(
                    List.of(summary1, summary2)
            );

            // When
            GetAllStaffAccountsResponseDto responseDto = sut.toGetAllResponseDto(queryResponse);

            // Then
            assertThat(responseDto.staffAccountSummaryResponseDtos()).hasSize(2);

            StaffAccountSummaryResponseDto dto1 = responseDto.staffAccountSummaryResponseDtos().get(0);
            assertThat(dto1.id()).isEqualTo(summary1.id());
            assertThat(dto1.username()).isEqualTo(summary1.username());
            assertThat(dto1.status()).isEqualTo(summary1.status());
            assertThat(dto1.orderAccessDuration()).isEqualTo(summary1.orderAccessDuration());
            assertThat(dto1.modmailTranscriptAccessDuration()).isEqualTo(summary1.modmailTranscriptAccessDuration());
            assertThat(dto1.createdAtUtc()).isEqualTo(summary1.createdAtUtc());

            StaffAccountSummaryResponseDto dto2 = responseDto.staffAccountSummaryResponseDtos().get(1);
            assertThat(dto2.id()).isEqualTo(summary2.id());
            assertThat(dto2.username()).isEqualTo(summary2.username());
            assertThat(dto2.status()).isEqualTo(summary2.status());
            assertThat(dto2.orderAccessDuration()).isEqualTo(summary2.orderAccessDuration());
            assertThat(dto2.modmailTranscriptAccessDuration()).isEqualTo(summary2.modmailTranscriptAccessDuration());
            assertThat(dto2.createdAtUtc()).isEqualTo(summary2.createdAtUtc());
        }

        @Test
        void shouldHandleEmptyList() {
            // Given
            GetAllStaffAccountsQueryResponse queryResponse = new GetAllStaffAccountsQueryResponse(List.of());

            // When
            GetAllStaffAccountsResponseDto responseDto = sut.toGetAllResponseDto(queryResponse);

            // Then
            assertThat(responseDto.staffAccountSummaryResponseDtos()).isEmpty();
        }
    }
}
