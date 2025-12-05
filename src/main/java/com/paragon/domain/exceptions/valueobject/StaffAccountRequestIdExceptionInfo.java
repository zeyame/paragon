package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class StaffAccountRequestIdExceptionInfo extends DomainExceptionInfo {
    private StaffAccountRequestIdExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static StaffAccountRequestIdExceptionInfo missingValue() {
        return new StaffAccountRequestIdExceptionInfo(
                "Staff account request ID cannot be null or empty.",
                118001
        );
    }
}
