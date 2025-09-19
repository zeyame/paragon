package com.paragon.domain.exceptions.aggregate;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class StaffAccountExceptionInfo extends DomainExceptionInfo {
    private StaffAccountExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static StaffAccountExceptionInfo missingUsername() {
        return new StaffAccountExceptionInfo("Username is required for registration.", 10001);
    }
}
