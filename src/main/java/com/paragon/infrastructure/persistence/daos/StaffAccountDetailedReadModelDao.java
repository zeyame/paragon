package com.paragon.infrastructure.persistence.daos;

import java.time.Instant;
import java.util.UUID;

public record StaffAccountDetailedReadModelDao(
        UUID id,
        String username,
        Integer orderAccessDurationInDays,
        Integer modmailTranscriptAccessDurationInDays,
        String status,
        Instant lockedUntilUtc,
        Instant lastLoginAtUtc,
        UUID createdBy,
        UUID disabledBy,
        Instant createdAtUtc
) {
}
