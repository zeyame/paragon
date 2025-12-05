package com.paragon.infrastructure.persistence.daos;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.TargetType;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryId;
import com.paragon.domain.models.valueobjects.TargetId;
import com.paragon.domain.models.valueobjects.StaffAccountId;

import java.time.Instant;
import java.util.UUID;

public record AuditTrailEntryDao(
        UUID id,
        UUID actorId,
        String actionType,
        String targetId,
        String targetType,
        Instant createdAtUtc
) {
    public AuditTrailEntry toAuditTrailEntry() {
        return AuditTrailEntry.createFrom(
                AuditEntryId.from(id.toString()),
                StaffAccountId.from(actorId.toString()),
                AuditEntryActionType.valueOf(actionType),
                targetId != null ? TargetId.of(targetId) : null,
                targetType != null ? TargetType.valueOf(targetType) : null
        );
    }
}