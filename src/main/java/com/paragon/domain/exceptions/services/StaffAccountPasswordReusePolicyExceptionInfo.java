package com.paragon.domain.exceptions.services;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class StaffAccountPasswordReusePolicyExceptionInfo extends DomainExceptionInfo {
    private StaffAccountPasswordReusePolicyExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static StaffAccountPasswordReusePolicyExceptionInfo passwordUsedWithinRestrictedWindow() {
        return new StaffAccountPasswordReusePolicyExceptionInfo(
                "The entered password was used recently and cannot be reused yet.",
                300001
        );
    }
}
