package com.paragon.api.dtos.staffaccount.getall;

import java.time.Instant;
import java.util.UUID;

public record StaffAccountSummaryResponseDto(
        UUID id,
        String username,
        String status,
        int orderAccessDuration,
        int modmailTranscriptAccessDuration,
        Instant createdAtUtc
) {

}
