package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.RefreshTokenHashException;
import com.paragon.domain.exceptions.valueobject.RefreshTokenHashExceptionInfo;
import com.paragon.domain.interfaces.TokenHasher;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class RefreshTokenHash extends ValueObject {
    private final String hashedValue;
    private final String plainValue;

    private RefreshTokenHash(String hashedValue, String plainValue) {
        this.hashedValue = hashedValue;
        this.plainValue = plainValue;
    }

    public static RefreshTokenHash generate(TokenHasher tokenHasher) {
        String plain = UUID.randomUUID().toString();
        String hashed = tokenHasher.hash(plain);
        return new RefreshTokenHash(hashed, plain);
    }

    public static RefreshTokenHash fromPlainToken(String plainToken, TokenHasher tokenHasher) {
        assertValidPlainToken(plainToken);
        String hashedValue = tokenHasher.hash(plainToken);
        return new RefreshTokenHash(hashedValue, plainToken);
    }

    public static RefreshTokenHash fromHashed(String hashedValue) {
        if (hashedValue == null || hashedValue.isEmpty()) {
            throw new RefreshTokenHashException(RefreshTokenHashExceptionInfo.missingValue());
        }
        return new RefreshTokenHash(hashedValue, null);
    }

    public String getValue() {
        return hashedValue; // For persistence
    }

    public String getPlainValue() {
        return plainValue;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(hashedValue);
    }

    private static void assertValidPlainToken(String plainToken) {
        if (plainToken == null || plainToken.isEmpty()) {
            throw new RefreshTokenHashException(RefreshTokenHashExceptionInfo.missingValue());
        }
        try {
            UUID.fromString(plainToken);
        } catch (IllegalArgumentException e) {
            throw new RefreshTokenHashException(RefreshTokenHashExceptionInfo.invalidFormat());
        }
    }
}