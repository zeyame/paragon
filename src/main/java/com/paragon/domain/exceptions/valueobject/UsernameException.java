package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class UsernameException extends DomainException {
    public UsernameException(UsernameExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
