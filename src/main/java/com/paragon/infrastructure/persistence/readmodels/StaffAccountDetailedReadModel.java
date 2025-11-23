package com.paragon.infrastructure.persistence.readmodels;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StaffAccountDetailedReadModel(
        UUID id,
        String username,
        int orderAccessDurationInDays,
        int modmailTranscriptAccessDurationInDays,
        String status,
        Instant lockedUntilUtc,
        Instant lastLoginAtUtc,
        UUID createdBy,
        UUID disabledBy,
        List<String> permissionCodes,
        Instant createdAtUtc
) {}
