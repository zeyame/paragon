package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PlaintextRefreshTokenException;
import com.paragon.domain.exceptions.valueobject.PlaintextRefreshTokenExceptionInfo;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Getter
public class PlaintextRefreshToken extends ValueObject {
    private final String value;

    private static final int TOKEN_BYTE_LENGTH = 48; // 48 bytes = 64 characters when base64 encoded

    private PlaintextRefreshToken(String value) {
        this.value = value;
    }

    public static PlaintextRefreshToken of(String plaintext) {
        assertValidToken(plaintext);
        return new PlaintextRefreshToken(plaintext);
    }

    public static PlaintextRefreshToken generate() {
        String plaintext = generatePlaintext();
        return new PlaintextRefreshToken(plaintext);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static String generatePlaintext() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        random.nextBytes(tokenBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private static void assertValidToken(String value) {
        if (value == null || value.isBlank()) {
            throw new PlaintextRefreshTokenException(PlaintextRefreshTokenExceptionInfo.missingValue());
        }
    }
}