package com.paragon.application.commands.enablestaffaccount;

public record EnableStaffAccountCommand(
        String staffAccountIdToBeEnabled,
        String requestingStaffAccountId
) {
}
