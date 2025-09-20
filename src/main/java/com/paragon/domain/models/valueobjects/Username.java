package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.UsernameException;
import com.paragon.domain.exceptions.valueobject.UsernameExceptionInfo;

import java.util.List;
import java.util.Set;

public class Username extends ValueObject {
    private final String value;
    private static final Set<String> RESERVED = Set.of(
            "admin", "root", "moderator", "system", "support"
    );

    private Username(String value) {
        this.value = value;
    }

    public static Username of(String value) {
        assertValidUsername(value);
        return new Username(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidUsername(String value) {
        if (value == null || value.isBlank()) {
            throw new UsernameException(UsernameExceptionInfo.missingValue());
        }
        if (value.length() < 3 || value.length() > 20) {
            throw new UsernameException(UsernameExceptionInfo.lengthOutOfRange());
        }
        if (!value.matches("^[A-Za-z0-9_]+$")) {
            throw new UsernameException(UsernameExceptionInfo.invalidCharacters());
        }
        if (value.contains("__")) {
            throw new UsernameException(UsernameExceptionInfo.consecutiveUnderscores());
        }
        if (!Character.isLetter(value.charAt(0))) {
            throw new UsernameException(UsernameExceptionInfo.mustStartWithALetter());
        }
        if (value.endsWith("_")) {
            throw new UsernameException(UsernameExceptionInfo.mustNotEndWithUnderscore());
        }
        if (RESERVED.contains(value.toLowerCase())) {
            throw new UsernameException(UsernameExceptionInfo.reservedWord());
        }
    }
}
