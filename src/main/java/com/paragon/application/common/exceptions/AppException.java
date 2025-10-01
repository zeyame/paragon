package com.paragon.application.common.exceptions;

import com.paragon.domain.exceptions.DomainException;
import lombok.Getter;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppException that = (AppException) o;
        return errorCode == that.errorCode &&
                getMessage().equals(that.getMessage()) &&
                statusCode == that.statusCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessage(), errorCode, statusCode);
    }
}
