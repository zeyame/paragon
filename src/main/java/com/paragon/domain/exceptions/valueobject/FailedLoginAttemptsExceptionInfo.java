package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class FailedLoginAttemptsExceptionInfo extends DomainExceptionInfo {
    private FailedLoginAttemptsExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static FailedLoginAttemptsExceptionInfo invalidAttemptNumber() {
        return new FailedLoginAttemptsExceptionInfo(
                "Failed login attempt number must be a value between 0 and 5",
                108001
        );
    }

    public static FailedLoginAttemptsExceptionInfo maxAttemptsReached() {
        return new FailedLoginAttemptsExceptionInfo(
                "Maximum number of failed login attempts has been reached.",
                108002
        );
    }
}
