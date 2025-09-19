package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class OrderAccessDurationException extends DomainException {
    public OrderAccessDurationException(OrderAccessDurationExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
