package com.paragon.domain.exceptions;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {
    protected final int domainErrorCode;

    protected DomainException(DomainExceptionInfo exceptionInfo) {
        super(exceptionInfo.message);
        this.domainErrorCode = exceptionInfo.domainErrorCode;
    }
}
