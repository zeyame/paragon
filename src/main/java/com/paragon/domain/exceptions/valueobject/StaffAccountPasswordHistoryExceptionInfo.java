package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class StaffAccountPasswordHistoryExceptionInfo extends DomainExceptionInfo {
    private StaffAccountPasswordHistoryExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static StaffAccountPasswordHistoryExceptionInfo mustContainEntries() {
        return new StaffAccountPasswordHistoryExceptionInfo(
                "Password history must contain at least one entry",
                117001
        );
    }

    public static StaffAccountPasswordHistoryExceptionInfo entriesMustBelongToSingleAccount() {
        return new StaffAccountPasswordHistoryExceptionInfo(
                "Password history entries must belong to the same staff account.",
                117002
        );
    }
}
