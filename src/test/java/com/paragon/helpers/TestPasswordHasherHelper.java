package com.paragon.helpers;

import com.paragon.infrastructure.security.BCryptPasswordHasher;

public final class TestPasswordHasherHelper {
    private static final BCryptPasswordHasher PASSWORD_HASHER = new BCryptPasswordHasher();

    private TestPasswordHasherHelper() {
    }

    public static String hash(String plainText) {
        return PASSWORD_HASHER.hash(plainText);
    }
}
