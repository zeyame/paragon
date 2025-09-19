package com.paragon.domain.exceptions;

public abstract class DomainExceptionInfo {
    protected final String message;
    protected final int domainErrorCode;

    protected DomainExceptionInfo(String message, int domainErrorCode) {
        this.message = message;
        this.domainErrorCode = domainErrorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getDomainErrorCode() {
        return domainErrorCode;
    }
}
