package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PasswordException;
import com.paragon.domain.exceptions.valueobject.PasswordExceptionInfo;
import com.paragon.domain.interfaces.PasswordHasher;
import lombok.Getter;

import java.util.List;

@Getter
public class Password extends ValueObject {
    private final String value;

    private Password(String value) {
        this.value = value;
    }

    /**
     * @deprecated Use fromPlainText() or fromHashed() instead for clarity
     */
    @Deprecated
    public static Password of(String value) {
        assertValidPassword(value);
        return new Password(value);
    }

    public static Password fromPlainText(String plainText, PasswordHasher passwordHasher) {
        assertValidPassword(plainText);
        String hashed = passwordHasher.hash(plainText);
        return new Password(hashed);
    }

    public static Password fromHashed(String hashedValue) {
        assertValidPassword(hashedValue);
        return new Password(hashedValue);
    }

    public boolean matches(String plainText, PasswordHasher passwordHasher) {
        return passwordHasher.verify(plainText, this.value);
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
