package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class DateTimeUtcException extends DomainException {
    public DateTimeUtcException(DateTimeUtcExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
