package com.paragon.domain.exceptions.entity;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class AuditTrailEntryExceptionInfo extends DomainExceptionInfo {
    private AuditTrailEntryExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static AuditTrailEntryExceptionInfo actorIdRequired() {
        return new AuditTrailEntryExceptionInfo(
                "ActorId is required for creating an AuditTrailEntry. The user performing the action must be identified.",
                201001
        );
    }

    public static AuditTrailEntryExceptionInfo actionTypeRequired() {
        return new AuditTrailEntryExceptionInfo(
                "ActionType is required for creating an AuditTrailEntry. The performed action must be explicitly identified.",
                201002
        );
    }

    public static AuditTrailEntryExceptionInfo outcomeRequired() {
        return new AuditTrailEntryExceptionInfo(
                "Outcome is required for creating an AuditTrailEntry. Each action must record whether it succeeded or failed.",
                201003
        );
    }

}
