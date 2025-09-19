package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class StaffAccountIdExceptionInfo extends DomainExceptionInfo {
    private StaffAccountIdExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static StaffAccountIdExceptionInfo missingValue() {
        return new StaffAccountIdExceptionInfo("Staff account ID cannot be null or empty.", 102001);
    }

    public static StaffAccountIdExceptionInfo invalidFormat() {
        return new StaffAccountIdExceptionInfo("Staff acocunt ID should be of valid UUID format.", 102002);
    }
}
