package com.paragon.application.commands.completetemporarystaffaccountpasswordchange;

public record CompleteTemporaryStaffAccountPasswordChangeCommandResponse(
        String id,
        String username,
        String status,
        int version
) {
}
