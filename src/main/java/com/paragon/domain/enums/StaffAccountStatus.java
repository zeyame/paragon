package com.paragon.domain.enums;

public enum StaffAccountStatus {
    PENDING_PASSWORD_CHANGE,
    ACTIVE,
    DISABLED,
    LOCKED;

    // TODO: Add exception handling with a new custom StaffAccountStatusException
    public static StaffAccountStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        return StaffAccountStatus.valueOf(value.trim().toUpperCase());
    }
}
