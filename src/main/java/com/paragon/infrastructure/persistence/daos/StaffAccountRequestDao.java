package com.paragon.infrastructure.persistence.daos;

import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.enums.TargetType;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.domain.models.valueobjects.*;

import java.time.Instant;
import java.util.UUID;

public record StaffAccountRequestDao(
        UUID id,
        UUID submittedBy,
        String requestType,
        String targetId,
        String targetType,
        String status,
        Instant submittedAtUtc,
        Instant expiresAtUtc,
        UUID approvedBy,
        Instant approvedAtUtc,
        UUID rejectedBy,
        Instant rejectedAtUtc,
        int version,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {
    public StaffAccountRequest toStaffAccountRequest() {
        return StaffAccountRequest.createFrom(
                StaffAccountRequestId.of(id),
                StaffAccountId.of(submittedBy),
                StaffAccountRequestType.valueOf(requestType),
                targetId != null ? TargetId.of(targetId) : null,
                targetType != null ? TargetType.valueOf(targetType) : null,
                StaffAccountRequestStatus.valueOf(status),
                DateTimeUtc.of(submittedAtUtc),
                DateTimeUtc.of(expiresAtUtc),
                approvedBy != null ? StaffAccountId.of(approvedBy) : null,
                approvedAtUtc != null ? DateTimeUtc.of(approvedAtUtc) : null,
                rejectedBy != null ? StaffAccountId.of(rejectedBy) : null,
                rejectedAtUtc != null ? DateTimeUtc.of(rejectedAtUtc) : null,
                Version.of(version)
        );
    }
}