package com.paragon.application.common.interfaces;

import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.PlaintextPassword;

public interface PasswordHasher {
    Password hash(PlaintextPassword plaintextPassword);
    boolean verify(PlaintextPassword enteredPassword, Password hashedPassword);
}
