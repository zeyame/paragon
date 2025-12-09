package com.paragon.domain.interfaces.services;

import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.PlaintextPassword;

public interface PasswordHasher {
    Password hash(PlaintextPassword plaintextPassword);
    boolean verify(String enteredPassword, Password hashedPassword);
}
