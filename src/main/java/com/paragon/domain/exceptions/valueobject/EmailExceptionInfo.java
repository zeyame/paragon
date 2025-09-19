package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class EmailExceptionInfo extends DomainExceptionInfo {
    private EmailExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static EmailExceptionInfo missingValue() {
        return new EmailExceptionInfo("Email address must not be null or empty.", 104001);
    }

    public static EmailExceptionInfo lengthOutOfRange() {
        return new EmailExceptionInfo("Email address must not exceed 320 characters.", 104002);
    }

    public static EmailExceptionInfo invalidFormat() {
        return new EmailExceptionInfo("Email address has an invalid format (must be a valid address like user@example.com).", 104003);
    }
}
