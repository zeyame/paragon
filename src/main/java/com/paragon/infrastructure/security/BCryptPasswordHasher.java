package com.paragon.infrastructure.security;

import com.paragon.application.common.interfaces.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasher() {
        this.encoder = new BCryptPasswordEncoder();
    }

    @Override
    public String hash(String plainText) {
        return encoder.encode(plainText);
    }

    @Override
    public boolean verify(String plainText, String hashedPassword) {
        return encoder.matches(plainText, hashedPassword);
    }
}