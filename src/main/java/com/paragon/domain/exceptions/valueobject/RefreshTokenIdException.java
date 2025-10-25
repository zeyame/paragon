package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class RefreshTokenIdException extends DomainException {
    public RefreshTokenIdException(RefreshTokenIdExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}