package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class FailedLoginAttemptsException extends DomainException {
    public FailedLoginAttemptsException(FailedLoginAttemptsExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
