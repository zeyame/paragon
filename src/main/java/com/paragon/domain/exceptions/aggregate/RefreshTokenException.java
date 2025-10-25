package com.paragon.domain.exceptions.aggregate;

import com.paragon.domain.exceptions.DomainException;

public class RefreshTokenException extends DomainException {
    public RefreshTokenException(RefreshTokenExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
