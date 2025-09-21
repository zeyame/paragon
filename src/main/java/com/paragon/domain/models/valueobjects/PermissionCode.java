package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PermissionCodeException;
import com.paragon.domain.exceptions.valueobject.PermissionCodeExceptionInfo;

import java.util.List;

public class PermissionCode extends ValueObject {
    private final String value;

    private PermissionCode(String value) {
        this.value = value;
    }

    public static PermissionCode of(String value) {
        assertValidPermissionCode(value);
        return new PermissionCode(value.toUpperCase());
    }

    public String getValue() {
        return value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidPermissionCode(String value) {
        if (value == null || value.isBlank()) {
            throw new PermissionCodeException(PermissionCodeExceptionInfo.missingValue());
        }
        if (value.length() < 3 || value.length() > 50) {
            throw new PermissionCodeException(PermissionCodeExceptionInfo.lengthOutOfRange());
        }
        if (!value.matches("^[A-Za-z_]+$")) {
            throw new PermissionCodeException(PermissionCodeExceptionInfo.invalidCharacters());
        }
        if (value.startsWith("_") || value.endsWith("_")) {
            throw new PermissionCodeException(PermissionCodeExceptionInfo.mustNotStartOrEndWithUnderscore());
        }
        if (value.contains("__")) {
            throw new PermissionCodeException(PermissionCodeExceptionInfo.consecutiveUnderscores());
        }
    }
}
