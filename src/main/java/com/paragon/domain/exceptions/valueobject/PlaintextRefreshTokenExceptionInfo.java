package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class PlaintextRefreshTokenExceptionInfo extends DomainExceptionInfo {
    private PlaintextRefreshTokenExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static PlaintextRefreshTokenExceptionInfo missingValue() {
        return new PlaintextRefreshTokenExceptionInfo(
                "Plaintext refresh token cannot be null or empty.",
                114001
        );
    }
}