package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PasswordException;
import com.paragon.domain.exceptions.valueobject.PasswordExceptionInfo;
import com.paragon.domain.interfaces.PasswordHasher;
import lombok.Getter;

import java.util.List;

@Getter
public class Password extends ValueObject {
    private final String value;

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

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
        if (hashedValue == null || hashedValue.isBlank()) {
            throw new PasswordException(PasswordExceptionInfo.missingValue());
        }
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

        if (value.length() < MIN_LENGTH) {
            throw new PasswordException(PasswordExceptionInfo.tooShort(MIN_LENGTH));
        }

        if (value.length() > MAX_LENGTH) {
            throw new PasswordException(PasswordExceptionInfo.tooLong(MAX_LENGTH));
        }

        if (value.chars().noneMatch(Character::isUpperCase)) {
            throw new PasswordException(PasswordExceptionInfo.missingUppercase());
        }

        if (value.chars().noneMatch(Character::isLowerCase)) {
            throw new PasswordException(PasswordExceptionInfo.missingLowercase());
        }

        if (value.chars().noneMatch(Character::isDigit)) {
            throw new PasswordException(PasswordExceptionInfo.missingDigit());
        }

        if (value.chars().noneMatch(ch -> SPECIAL_CHARACTERS.indexOf(ch) >= 0)) {
            throw new PasswordException(PasswordExceptionInfo.missingSpecialCharacter());
        }

        if (value.chars().anyMatch(Character::isWhitespace)) {
            throw new PasswordException(PasswordExceptionInfo.containsWhitespace());
        }
    }
}
