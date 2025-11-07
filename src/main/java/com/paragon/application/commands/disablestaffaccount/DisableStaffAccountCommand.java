package com.paragon.application.commands.disablestaffaccount;

public record DisableStaffAccountCommand(
        String staffAccountIdToBeDisabled,
        String requestingStaffAccountId
) {
}
