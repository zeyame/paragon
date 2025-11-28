package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class PasswordHistoryEntryExceptionInfo extends DomainExceptionInfo {
    private PasswordHistoryEntryExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static PasswordHistoryEntryExceptionInfo missingStaffAccountId() {
        return new PasswordHistoryEntryExceptionInfo(
                "Staff account ID is required for password history entries.",
                116001
        );
    }

    public static PasswordHistoryEntryExceptionInfo missingHashedPassword() {
        return new PasswordHistoryEntryExceptionInfo(
                "Password history entries require a hashed password value.",
                116002
        );
    }

    public static PasswordHistoryEntryExceptionInfo missingChangedAtTimestamp() {
        return new PasswordHistoryEntryExceptionInfo(
                "Password history entries require the timestamp when the password was changed.",
                116003
        );
    }
}
