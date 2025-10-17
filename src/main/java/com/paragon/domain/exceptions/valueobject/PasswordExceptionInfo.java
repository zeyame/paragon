package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class PasswordExceptionInfo extends DomainExceptionInfo {
    private PasswordExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static PasswordExceptionInfo missingValue() {
        return new PasswordExceptionInfo(
                "Password cannot be null or empty.",
                105001
        );
    }

    public static PasswordExceptionInfo tooShort(int minLength) {
        return new PasswordExceptionInfo(
                String.format("Password must be at least %d characters long.", minLength),
                105002
        );
    }

    public static PasswordExceptionInfo tooLong(int maxLength) {
        return new PasswordExceptionInfo(
                String.format("Password must not exceed %d characters.", maxLength),
                105003
        );
    }

    public static PasswordExceptionInfo missingUppercase() {
        return new PasswordExceptionInfo(
                "Password must contain at least one uppercase letter.",
                105004
        );
    }

    public static PasswordExceptionInfo missingLowercase() {
        return new PasswordExceptionInfo(
                "Password must contain at least one lowercase letter.",
                105005
        );
    }

    public static PasswordExceptionInfo missingDigit() {
        return new PasswordExceptionInfo(
                "Password must contain at least one digit.",
                105006
        );
    }

    public static PasswordExceptionInfo missingSpecialCharacter() {
        return new PasswordExceptionInfo(
                "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?).",
                105007
        );
    }

    public static PasswordExceptionInfo containsWhitespace() {
        return new PasswordExceptionInfo(
                "Password must not contain whitespace characters.",
                105008
        );
    }
}
