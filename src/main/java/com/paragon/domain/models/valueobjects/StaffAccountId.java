package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.StaffAccountIdException;
import com.paragon.domain.exceptions.valueobject.StaffAccountIdExceptionInfo;

import java.util.List;
import java.util.UUID;

public class StaffAccountId extends ValueObject {
    private final UUID value;

    private StaffAccountId(UUID value) {
        this.value = value;
    }

    public static StaffAccountId of(UUID value) {
        if (value == null) {
            throw new StaffAccountIdException(StaffAccountIdExceptionInfo.missingValue());
        }
        return new StaffAccountId(value);
    }

    public static StaffAccountId from(String rawId) {
        assertValidStaffAccountId(rawId);
        return new StaffAccountId(UUID.fromString(rawId));
    }

    public static StaffAccountId generate() {
        return new StaffAccountId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidStaffAccountId(String rawId) {
        if (rawId == null || rawId.isEmpty()) {
            throw new StaffAccountIdException(StaffAccountIdExceptionInfo.missingValue());
        }
        try {
            UUID.fromString(rawId);
        } catch (IllegalArgumentException e) {
            throw new StaffAccountIdException(StaffAccountIdExceptionInfo.invalidFormat());
        }
    }
}
