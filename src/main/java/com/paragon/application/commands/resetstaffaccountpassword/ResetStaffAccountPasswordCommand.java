package com.paragon.application.commands.resetstaffaccountpassword;

public record ResetStaffAccountPasswordCommand(
        String staffAccountIdToReset,
        String requestingStaffAccountId
) {
}
