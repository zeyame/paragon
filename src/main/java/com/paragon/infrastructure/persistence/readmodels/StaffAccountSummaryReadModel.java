package com.paragon.infrastructure.persistence.readmodels;

import java.time.Instant;
import java.util.UUID;

public record StaffAccountSummaryReadModel(
        UUID id,
        String username,
        String status,
        int orderAccessDuration,
        int modmailTranscriptAccessDuration,
        Instant createdAtUtc
) {}

