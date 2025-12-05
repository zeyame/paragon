package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.StaffAccountRequestIdException;
import com.paragon.domain.exceptions.valueobject.StaffAccountRequestIdExceptionInfo;

import java.util.UUID;

public record StaffAccountRequestId(UUID value) {
    public StaffAccountRequestId {
        if (value == null) {
            throw new StaffAccountRequestIdException(StaffAccountRequestIdExceptionInfo.missingValue());
        }
    }

    public static StaffAccountRequestId of(UUID value) {
        if (value == null) {
            throw new StaffAccountRequestIdException(StaffAccountRequestIdExceptionInfo.missingValue());
        }
        return new StaffAccountRequestId(value);
    }

    public static StaffAccountRequestId from(String rawId) {
        assertValidStaffAccountRequestId(rawId);
        return new StaffAccountRequestId(UUID.fromString(rawId));
    }

    public static StaffAccountRequestId generate() {
        return new StaffAccountRequestId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    private static void assertValidStaffAccountRequestId(String rawId) {
        if (rawId == null || rawId.isEmpty()) {
            throw new StaffAccountRequestIdException(StaffAccountRequestIdExceptionInfo.missingValue());
        }
        try {
            UUID.fromString(rawId);
        } catch (IllegalArgumentException e) {
            throw new StaffAccountRequestIdException(StaffAccountRequestIdExceptionInfo.invalidFormat());
        }
    }
}
