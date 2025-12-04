package com.paragon.application.commands.completetemporarystaffaccountpasswordchange;

public record CompleteTemporaryStaffAccountPasswordChangeCommand(
        String id,
        String newPassword
) {
}
