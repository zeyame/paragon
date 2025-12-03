package com.paragon.application.commands.completetemporarystaffaccountpasswordchange;

public record CompleteTemporaryStaffAccountPasswordChangeResponse(
        String id,
        String username,
        String status,
        int version
) {
}
