package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class PasswordException extends DomainException {
    public PasswordException(PasswordExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
