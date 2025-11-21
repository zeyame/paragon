package com.paragon.application.queries.getstaffaccountbyusername;

import java.time.Instant;
import java.util.UUID;

public record GetStaffAccountByUsernameQueryResponse(
        UUID id,
        String username,
        String status,
        int orderAccessDuration,
        int modmailTranscriptAccessDuration,
        Instant createdAt
) {
}
