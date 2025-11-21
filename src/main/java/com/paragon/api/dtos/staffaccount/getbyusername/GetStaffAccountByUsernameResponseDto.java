package com.paragon.api.dtos.staffaccount.getbyusername;

import java.time.Instant;
import java.util.UUID;

public record GetStaffAccountByUsernameResponseDto(
        UUID id,
        String username,
        String status,
        int orderAccessDuration,
        int modmailTranscriptAccessDuration,
        Instant createdAt
) {
}
