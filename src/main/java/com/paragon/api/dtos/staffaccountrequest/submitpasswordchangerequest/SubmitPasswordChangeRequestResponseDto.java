package com.paragon.api.dtos.staffaccountrequest.submitpasswordchangerequest;

public record SubmitPasswordChangeRequestResponseDto(
        String requestId,
        String submittedBy,
        String requestType,
        String status,
        String submittedAtUtc,
        String expiresAtUtc
) {
}
