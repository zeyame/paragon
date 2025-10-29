package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class IpAddressException extends DomainException {
    public IpAddressException(IpAddressExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}