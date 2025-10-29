package com.paragon.domain.exceptions.aggregate;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class RefreshTokenExceptionInfo extends DomainExceptionInfo {
    private RefreshTokenExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static RefreshTokenExceptionInfo staffAccountIdRequired() {
        return new RefreshTokenExceptionInfo(
                "A staff account id is required to generate a refresh token.",
                20001
        );
    }

    public static RefreshTokenExceptionInfo tokenAlreadyRevoked() {
        return new RefreshTokenExceptionInfo(
                "Refresh token has already been revoked.",
                20002
        );
    }

    public static RefreshTokenExceptionInfo ipAddressRequired() {
        return new RefreshTokenExceptionInfo(
                "An IP address is required to issue a refresh token.",
                20003
        );
    }
}
