package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PasswordHistoryEntryException;
import com.paragon.domain.exceptions.valueobject.PasswordHistoryEntryExceptionInfo;

public record PasswordHistoryEntry(
        StaffAccountId staffAccountId,
        Password hashedPassword,
        boolean isTemporary,
        DateTimeUtc changedAt
) {
    public PasswordHistoryEntry {
        if (staffAccountId == null) {
            throw new PasswordHistoryEntryException(
                    PasswordHistoryEntryExceptionInfo.missingStaffAccountId()
            );
        }
        if (hashedPassword == null) {
            throw new PasswordHistoryEntryException(
                    PasswordHistoryEntryExceptionInfo.missingHashedPassword()
            );
        }
        if (changedAt == null) {
            throw new PasswordHistoryEntryException(
                    PasswordHistoryEntryExceptionInfo.missingChangedAtTimestamp()
            );
        }
    }
}
