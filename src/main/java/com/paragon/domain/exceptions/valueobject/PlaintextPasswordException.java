package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class PlaintextPasswordException extends DomainException {
    public PlaintextPasswordException(PlaintextPasswordExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}