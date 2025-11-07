package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class PlaintextPasswordExceptionInfo extends DomainExceptionInfo {
    private PlaintextPasswordExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static PlaintextPasswordExceptionInfo missingValue() {
        return new PlaintextPasswordExceptionInfo(
                "Plaintext password cannot be null or empty.",
                113001
        );
    }

    public static PlaintextPasswordExceptionInfo tooShort(int minLength) {
        return new PlaintextPasswordExceptionInfo(
                String.format("Password must be at least %d characters long.", minLength),
                113002
        );
    }

    public static PlaintextPasswordExceptionInfo tooLong(int maxLength) {
        return new PlaintextPasswordExceptionInfo(
                String.format("Password must not exceed %d characters.", maxLength),
                113003
        );
    }

    public static PlaintextPasswordExceptionInfo missingUppercase() {
        return new PlaintextPasswordExceptionInfo(
                "Password must contain at least one uppercase letter.",
                113004
        );
    }

    public static PlaintextPasswordExceptionInfo missingLowercase() {
        return new PlaintextPasswordExceptionInfo(
                "Password must contain at least one lowercase letter.",
                113005
        );
    }

    public static PlaintextPasswordExceptionInfo missingDigit() {
        return new PlaintextPasswordExceptionInfo(
                "Password must contain at least one digit.",
                113006
        );
    }

    public static PlaintextPasswordExceptionInfo missingSpecialCharacter() {
        return new PlaintextPasswordExceptionInfo(
                "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?).",
                113007
        );
    }

    public static PlaintextPasswordExceptionInfo containsWhitespace() {
        return new PlaintextPasswordExceptionInfo(
                "Password must not contain whitespace characters.",
                113008
        );
    }
}