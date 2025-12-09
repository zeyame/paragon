package com.paragon.infrastructure.persistence.readmodels;

import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.enums.TargetType;

import java.time.Instant;
import java.util.UUID;

public record StaffAccountRequestReadModel(
        UUID id,
        UUID submittedBy,
        String submittedByUsername,
        StaffAccountRequestType requestType,
        String targetId,
        TargetType targetType,
        StaffAccountRequestStatus status,
        Instant submittedAtUtc,
        Instant expiresAtUtc,
        UUID approvedBy,
        String approvedByUsername,
        Instant approvedAtUtc,
        UUID rejectedBy,
        String rejectedByUsername,
        Instant rejectedAtUtc
) {
}
