package com.paragon.application.commands.submitstaffaccountpasswordchangerequest;

public record SubmitPasswordChangeRequestCommandResponse(
        String requestId,
        String submittedBy,
        String requestType,
        String status,
        String submittedAtUtc,
        String expiresAtUtc
) {
}