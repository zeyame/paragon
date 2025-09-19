package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class PasswordExceptionInfo extends DomainExceptionInfo {
    private PasswordExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static PasswordExceptionInfo missingValue() {
        return new PasswordExceptionInfo("Password cannot be null or empty.", 105001);
    }
}
