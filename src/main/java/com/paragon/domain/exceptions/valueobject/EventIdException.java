package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class EventIdException extends DomainException {
    public EventIdException(EventIdExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
