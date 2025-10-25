package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class RefreshTokenHashException extends DomainException {
    public RefreshTokenHashException(RefreshTokenHashExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}