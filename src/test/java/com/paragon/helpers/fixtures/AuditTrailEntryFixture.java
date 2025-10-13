package com.paragon.helpers.fixtures;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.enums.Outcome;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryTargetId;
import com.paragon.domain.models.valueobjects.StaffAccountId;

import java.util.UUID;

public class AuditTrailEntryFixture {
    private String actorId = UUID.randomUUID().toString();
    private AuditEntryActionType actionType = AuditEntryActionType.REGISTER_ACCOUNT;
    private String targetId = UUID.randomUUID().toString();
    private AuditEntryTargetType targetType = AuditEntryTargetType.ACCOUNT;
    private Outcome outcome = Outcome.SUCCESS;
    private String ipAddress = "192.168.1.1";
    private String correlationId = UUID.randomUUID().toString();

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

    public AuditTrailEntryFixture withOutcome(Outcome value) {
        this.outcome = value;
        return this;
    }

    public AuditTrailEntryFixture withIpAddress(String value) {
        this.ipAddress = value;
        return this;
    }

    public AuditTrailEntryFixture withCorrelationId(String value) {
        this.correlationId = value;
        return this;
    }

    public AuditTrailEntry build() {
        return AuditTrailEntry.create(
                StaffAccountId.from(actorId),
                actionType,
                targetId != null ? AuditEntryTargetId.of(targetId) : null,
                targetType,
                outcome,
                ipAddress,
                correlationId
        );
    }

    public static AuditTrailEntry validAuditTrailEntry() {
        return new AuditTrailEntryFixture().build();
    }
}