package com.paragon.domain.models.entities;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.TargetType;
import com.paragon.domain.exceptions.entity.AuditTrailEntryException;
import com.paragon.domain.exceptions.entity.AuditTrailEntryExceptionInfo;
import com.paragon.domain.models.valueobjects.AuditEntryId;
import com.paragon.domain.models.valueobjects.TargetId;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import lombok.Getter;

@Getter
public class AuditTrailEntry extends Entity<AuditEntryId> {
    private final StaffAccountId actorId;
    private final AuditEntryActionType actionType;
    private final TargetId targetId;
    private final TargetType targetType;

    private AuditTrailEntry(AuditEntryId id, StaffAccountId actorId, AuditEntryActionType actionType, TargetId targetId, TargetType targetType) {
        super(id);
        this.actorId = actorId;
        this.actionType = actionType;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    public static AuditTrailEntry create(StaffAccountId actorId, AuditEntryActionType actionType, TargetId targetId,
                                         TargetType targetType
    ) {
        assertValidAuditTrailEntry(actorId, actionType);
        return new AuditTrailEntry(
                AuditEntryId.generate(),
                actorId,
                actionType,
                targetId,
                targetType
        );
    }

    public static AuditTrailEntry createFrom(AuditEntryId id, StaffAccountId actorId, AuditEntryActionType actionType,
                                             TargetId targetId, TargetType targetType
    ) {
        return new AuditTrailEntry(
                id,
                actorId,
                actionType,
                targetId,
                targetType
        );
    }

    private static void assertValidAuditTrailEntry(StaffAccountId actorId, AuditEntryActionType actionType) {
        if (actorId == null) {
            throw new AuditTrailEntryException(AuditTrailEntryExceptionInfo.actorIdRequired());
        }
        if (actionType == null) {
            throw new AuditTrailEntryException(AuditTrailEntryExceptionInfo.actionTypeRequired());
        }
    }
}
