package com.paragon.domain.exceptions.services;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class RefreshTokenRevocationServiceExceptionInfo extends DomainExceptionInfo {
    private RefreshTokenRevocationServiceExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static RefreshTokenRevocationServiceExceptionInfo noActiveTokensFound() {
        return new RefreshTokenRevocationServiceExceptionInfo(
                "No active refresh tokens found for the specified staff account.",
                300001
        );
    }
}
