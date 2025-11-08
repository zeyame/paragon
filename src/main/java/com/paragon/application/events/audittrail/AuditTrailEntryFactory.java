package com.paragon.application.events.audittrail;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.events.EventNames;
import com.paragon.domain.events.staffaccountevents.*;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryTargetId;

public final class AuditTrailEntryFactory {

    private AuditTrailEntryFactory() {}

    public static AuditTrailEntry fromStaffAccountEvent(StaffAccountEventBase event) {
        return switch (event.getEventName()) {
            case EventNames.STAFF_ACCOUNT_REGISTERED -> fromStaffAccountRegisteredEvent((StaffAccountRegisteredEvent) event);
            case EventNames.STAFF_ACCOUNT_LOCKED -> fromStaffAccountLockedEvent((StaffAccountLockedEvent) event);
            case EventNames.STAFF_ACCOUNT_LOGGED_IN -> fromStaffAccountLoggedInEvent((StaffAccountLoggedInEvent) event);
            case EventNames.STAFF_ACCOUNT_DISABLED -> fromStaffAccountDisabledEvent((StaffAccountDisabledEvent) event);
            case EventNames.STAFF_ACCOUNT_ENABLED -> fromStaffAccountEnabledEvent((StaffAccountEnabledEvent) event);
            case EventNames.STAFF_ACCOUNT_PASSWORD_RESET -> fromStaffAccountPasswordResetEvent((StaffAccountPasswordResetEvent) event);
            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getEventName());
        };
    }

    private static AuditTrailEntry fromStaffAccountRegisteredEvent(StaffAccountRegisteredEvent event) {
        return AuditTrailEntry.create(
                event.getStaffAccountCreatedBy(),
                AuditEntryActionType.REGISTER_ACCOUNT,
                AuditEntryTargetId.of(event.getStaffAccountId().getValue().toString()),
                AuditEntryTargetType.ACCOUNT
        );
    }

    private static AuditTrailEntry fromStaffAccountLockedEvent(StaffAccountLockedEvent event) {
        return AuditTrailEntry.create(
                event.getStaffAccountId(),
                AuditEntryActionType.ACCOUNT_LOCKED,
                AuditEntryTargetId.of(event.getStaffAccountId().getValue().toString()),
                AuditEntryTargetType.ACCOUNT
        );
    }

    private static AuditTrailEntry fromStaffAccountLoggedInEvent(StaffAccountLoggedInEvent event) {
        return AuditTrailEntry.create(
                event.getStaffAccountId(),
                AuditEntryActionType.LOGIN,
                AuditEntryTargetId.of(event.getStaffAccountId().getValue().toString()),
                AuditEntryTargetType.ACCOUNT
        );
    }

    private static AuditTrailEntry fromStaffAccountDisabledEvent(StaffAccountDisabledEvent event) {
        return AuditTrailEntry.create(
                event.getStaffAccountDisabledBy(),
                AuditEntryActionType.DISABLE_ACCOUNT,
                AuditEntryTargetId.of(event.getStaffAccountId().getValue().toString()),
                AuditEntryTargetType.ACCOUNT
        );
    }

    private static AuditTrailEntry fromStaffAccountPasswordResetEvent(StaffAccountPasswordResetEvent event) {
        return AuditTrailEntry.create(
                event.getStaffAccountPasswordResetBy(),
                AuditEntryActionType.RESET_ACCOUNT_PASSWORD,
                AuditEntryTargetId.of(event.getStaffAccountId().getValue().toString()),
                AuditEntryTargetType.ACCOUNT
        );
    }

    private static AuditTrailEntry fromStaffAccountEnabledEvent(StaffAccountEnabledEvent event) {
        return AuditTrailEntry.create(
                event.getStaffAccountEnabledBy(),
                AuditEntryActionType.ENABLE_ACCOUNT,
                AuditEntryTargetId.of(event.getStaffAccountId().getValue().toString()),
                AuditEntryTargetType.ACCOUNT
        );
    }
}