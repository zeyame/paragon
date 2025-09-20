package com.paragon.domain.exceptions;

import lombok.Getter;

@Getter
public abstract class DomainExceptionInfo {
    protected final String message;
    protected final int domainErrorCode;

    protected DomainExceptionInfo(String message, int domainErrorCode) {
        this.message = message;
        this.domainErrorCode = domainErrorCode;
    }
}
