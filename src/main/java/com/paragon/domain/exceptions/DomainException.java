package com.paragon.domain.exceptions;

public abstract class DomainException extends RuntimeException {
    protected final int domainErrorCode;

    protected DomainException(DomainExceptionInfo exceptionInfo) {
        super(exceptionInfo.message);
        this.domainErrorCode = exceptionInfo.domainErrorCode;
    }
}
