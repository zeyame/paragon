package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class PlaintextRefreshTokenException extends DomainException {
    public PlaintextRefreshTokenException(PlaintextRefreshTokenExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}