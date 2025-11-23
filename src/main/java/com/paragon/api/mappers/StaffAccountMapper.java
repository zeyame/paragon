package com.paragon.api.mappers;

import com.paragon.api.dtos.staffaccount.disable.DisableStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.enable.EnableStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.getall.StaffAccountSummaryResponseDto;
import com.paragon.api.dtos.staffaccount.getbyusername.GetStaffAccountByUsernameResponseDto;
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
import com.paragon.application.queries.getstaffaccountbyusername.GetStaffAccountByUsernameQueryResponse;

import java.util.Optional;

public class StaffAccountMapper {
    public static RegisterStaffAccountCommand toRegisterCommand(RegisterStaffAccountRequestDto requestDto, String requestingStaffAccountId) {
        return new RegisterStaffAccountCommand(
                requestDto.username(),
                requestDto.email(),
                requestDto.orderAccessDuration(),
                requestDto.modmailTranscriptAccessDuration(),
                requestDto.permissionCodes(),
                requestingStaffAccountId
        );
    }

    public static RegisterStaffAccountResponseDto toRegisterResponseDto(RegisterStaffAccountCommandResponse commandResponse) {
        return new RegisterStaffAccountResponseDto(
                commandResponse.id(),
                commandResponse.username(),
                commandResponse.tempPassword(),
                commandResponse.status(),
                commandResponse.version()
        );
    }

    public static DisableStaffAccountCommand toDisableCommand(String staffAccountIdToBeDisabled, String requestingStaffAccountId) {
        return new DisableStaffAccountCommand(staffAccountIdToBeDisabled, requestingStaffAccountId);
    }

    public static DisableStaffAccountResponseDto toDisableResponseDto(DisableStaffAccountCommandResponse commandResponse) {
        return new DisableStaffAccountResponseDto(
                commandResponse.id(),
                commandResponse.status(),
                commandResponse.disabledBy(),
                commandResponse.version()
        );
    }

    public static EnableStaffAccountCommand toEnableCommand(String staffAccountIdToBeEnabled, String requestingStaffAccountId) {
        return new EnableStaffAccountCommand(staffAccountIdToBeEnabled, requestingStaffAccountId);
    }

    public static EnableStaffAccountResponseDto toEnableResponseDto(EnableStaffAccountCommandResponse commandResponse) {
        return new EnableStaffAccountResponseDto(
                commandResponse.id(),
                commandResponse.status(),
                commandResponse.enabledBy(),
                commandResponse.version()
        );
    }

    public static ResetStaffAccountPasswordCommand toResetPasswordCommand(String staffAccountIdToReset, String requestingStaffAccountId) {
        return new ResetStaffAccountPasswordCommand(staffAccountIdToReset, requestingStaffAccountId);
    }

    public static ResetStaffAccountPasswordResponseDto toResetPasswordResponseDto(ResetStaffAccountPasswordCommandResponse commandResponse) {
        return new ResetStaffAccountPasswordResponseDto(
                commandResponse.id(),
                commandResponse.temporaryPassword(),
                commandResponse.status(),
                commandResponse.passwordIssuedAt(),
                commandResponse.version()
        );
    }

    public static GetAllStaffAccountsResponseDto toGetAllResponseDto(GetAllStaffAccountsQueryResponse queryResponse) {
        return new GetAllStaffAccountsResponseDto(
                queryResponse.staffAccountSummaries()
                        .stream()
                        .map(StaffAccountMapper::toStaffAccountSummaryResponseDto)
                        .toList()
        );
    }

    private static StaffAccountSummaryResponseDto toStaffAccountSummaryResponseDto(StaffAccountSummary staffAccountSummary) {
        return new StaffAccountSummaryResponseDto(
                staffAccountSummary.id(),
                staffAccountSummary.username(),
                staffAccountSummary.status(),
                staffAccountSummary.createdAtUtc()
        );
    }

    public static GetStaffAccountByUsernameResponseDto toGetStaffAccountByUsernameResponseDto(GetStaffAccountByUsernameQueryResponse queryResponse) {
        return queryResponse.staffAccountSummary()
                .map(summary -> new GetStaffAccountByUsernameResponseDto(
                        summary.id(),
                        summary.username(),
                        summary.status(),
                        summary.createdAtUtc()
                ))
                .orElseGet(GetStaffAccountByUsernameResponseDto::empty);
    }
}
