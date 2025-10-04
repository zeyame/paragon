package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PermissionIdException;
import com.paragon.domain.exceptions.valueobject.PermissionIdExceptionInfo;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class PermissionId extends ValueObject {

    private final UUID value;

    private PermissionId(UUID value) {
        this.value = value;
    }

    public static PermissionId of(UUID value) {
        if (value == null) {
            throw new PermissionIdException(PermissionIdExceptionInfo.missingValue());
        }
        return new PermissionId(value);
    }

    public static PermissionId from(String value) {
        assertValidPermissionId(value);
        return new PermissionId(UUID.fromString(value));
    }

    public static PermissionId generate() {
        return new PermissionId(UUID.randomUUID());
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidPermissionId(String value) {
        if (value == null || value.isEmpty()) {
            throw new PermissionIdException(PermissionIdExceptionInfo.missingValue());
        }
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new PermissionIdException(PermissionIdExceptionInfo.invalidFormat());
        }
    }
}
