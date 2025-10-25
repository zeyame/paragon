package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class RefreshTokenIdExceptionInfo extends DomainExceptionInfo {
    private RefreshTokenIdExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static RefreshTokenIdExceptionInfo missingValue() {
        return new RefreshTokenIdExceptionInfo(
                "Refresh token ID cannot be null or empty.",
                113001
        );
    }

    public static RefreshTokenIdExceptionInfo invalidFormat() {
        return new RefreshTokenIdExceptionInfo(
                "Refresh token ID should be of valid UUID format.",
                113002
        );
    }
}