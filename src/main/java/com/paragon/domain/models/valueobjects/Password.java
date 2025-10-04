package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PasswordException;
import com.paragon.domain.exceptions.valueobject.PasswordExceptionInfo;
import lombok.Getter;

import java.util.List;

/**
 * Password VO always represents a hashed password
 */
@Getter
public class Password extends ValueObject {
    private final String value;

    private Password(String value) {
        this.value = value;
    }

    public static Password of(String value) {
        assertValidPassword(value);
        return new Password(value);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidPassword(String value) {
        if (value == null || value.isBlank()) {
            throw new PasswordException(PasswordExceptionInfo.missingValue());
        }
    }
}
