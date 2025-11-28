package com.paragon.domain.exceptions.services;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class PasswordReusePolicyExceptionInfo extends DomainExceptionInfo {
    private PasswordReusePolicyExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static PasswordReusePolicyExceptionInfo passwordUsedWithinRestrictedWindow() {
        return new PasswordReusePolicyExceptionInfo(
                "The entered password was used recently and cannot be reused yet.",
                300001
        );
    }
}
