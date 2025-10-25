package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class RefreshTokenHashExceptionInfo extends DomainExceptionInfo {
    private RefreshTokenHashExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static RefreshTokenHashExceptionInfo missingValue() {
        return new RefreshTokenHashExceptionInfo(
                "Refresh token hash cannot be null or empty.",
                114001
        );
    }
}