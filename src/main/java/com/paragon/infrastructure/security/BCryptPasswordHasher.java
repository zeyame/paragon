package com.paragon.infrastructure.security;

import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.PlaintextPassword;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasher() {
        this.encoder = new BCryptPasswordEncoder();
    }

    @Override
    public Password hash(PlaintextPassword plaintextPassword) {
        String encodedPassword = encoder.encode(plaintextPassword.getValue());
        return Password.of(encodedPassword);
    }

    @Override
    public boolean verify(String plaintextPassword, Password hashedPassword) {
        return encoder.matches(plaintextPassword, hashedPassword.getValue());
    }
}