package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.RefreshTokenHashException;
import com.paragon.domain.exceptions.valueobject.RefreshTokenHashExceptionInfo;
import com.paragon.domain.interfaces.TokenHasher;
import lombok.Getter;

import java.util.List;

@Getter
public class RefreshTokenHash extends ValueObject {
    private final String value;

    private RefreshTokenHash(String value) {
        this.value = value;
    }

    public static RefreshTokenHash fromPlainToken(String plainToken, TokenHasher tokenHasher) {
        assertValidToken(plainToken);
        String hashedValue = tokenHasher.hash(plainToken);
        return new RefreshTokenHash(hashedValue);
    }

    public static RefreshTokenHash fromHashed(String hashedValue) {
        assertValidToken(hashedValue);
        return new RefreshTokenHash(hashedValue);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new RefreshTokenHashException(RefreshTokenHashExceptionInfo.missingValue());
        }
    }
}