package com.paragon.helpers;

import com.paragon.domain.models.valueobjects.PlaintextRefreshToken;
import com.paragon.infrastructure.security.SHA256TokenHasher;

public final class TestRefreshTokenHasherHelper {
    private static final SHA256TokenHasher TOKEN_HASHER = new SHA256TokenHasher();

    private TestRefreshTokenHasherHelper() { }

    public static String hash(String plaintext) {
        return TOKEN_HASHER.hash(PlaintextRefreshToken.of(plaintext)).getValue();
    }
}
