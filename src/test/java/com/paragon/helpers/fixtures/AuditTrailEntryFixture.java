package com.paragon.helpers.fixtures;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryTargetId;
import com.paragon.domain.models.valueobjects.StaffAccountId;

import java.util.UUID;

public class AuditTrailEntryFixture {
    private String actorId = UUID.randomUUID().toString();
    private AuditEntryActionType actionType = AuditEntryActionType.REGISTER_ACCOUNT;
    private String targetId = UUID.randomUUID().toString();
    private AuditEntryTargetType targetType = AuditEntryTargetType.ACCOUNT;

    public AuditTrailEntryFixture withActorId(String value) {
        this.actorId = value;
        return this;
    }

    public AuditTrailEntryFixture withActionType(AuditEntryActionType value) {
        this.actionType = value;
        return this;
    }

    public AuditTrailEntryFixture withTargetId(String value) {
        this.targetId = value;
        return this;
    }

    public AuditTrailEntryFixture withTargetType(AuditEntryTargetType value) {
        this.targetType = value;
        return this;
    }

    public AuditTrailEntry build() {
        return AuditTrailEntry.create(
                StaffAccountId.from(actorId),
                actionType,
                targetId != null ? AuditEntryTargetId.of(targetId) : null,
                targetType
        );
    }

    public static AuditTrailEntry validAuditTrailEntry() {
        return new AuditTrailEntryFixture().build();
    }
}