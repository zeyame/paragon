package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class TargetIdException extends DomainException {
    public TargetIdException(TargetIdExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
