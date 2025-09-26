package com.paragon.application.common.exceptions;

import com.paragon.domain.exceptions.DomainException;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final int errorCode;
    private final AppExceptionStatusCode statusCode;

    public AppException(DomainException domainException, AppExceptionStatusCode statusCode) {
        super(domainException.getMessage());
        this.errorCode = domainException.getDomainErrorCode();
        this.statusCode = statusCode;
    }

    public AppException(AppExceptionInfo appExceptionInfo) {
        super(appExceptionInfo.getMessage());
        this.errorCode = appExceptionInfo.getAppErrorCode();
        this.statusCode = appExceptionInfo.getStatusCode();
    }
}
