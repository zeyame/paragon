package com.paragon.domain.exceptions.services;

import com.paragon.domain.exceptions.DomainException;

public class RefreshTokenRevocationServiceException extends DomainException {
    public RefreshTokenRevocationServiceException(RefreshTokenRevocationServiceExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
