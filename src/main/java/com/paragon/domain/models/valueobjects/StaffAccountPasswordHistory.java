package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.StaffAccountPasswordHistoryException;
import com.paragon.domain.exceptions.valueobject.StaffAccountPasswordHistoryExceptionInfo;

import java.util.List;

public record StaffAccountPasswordHistory(List<PasswordHistoryEntry> entries) {
    public StaffAccountPasswordHistory(List<PasswordHistoryEntry> entries) {
        assertValidPasswordHistory(entries);
        this.entries = List.copyOf(entries);
    }

    public List<PasswordHistoryEntry> entriesOnOrAfter(DateTimeUtc cutOffDate) {
        return entries.stream()
                .filter(entry -> !entry.changedAt().isBefore(cutOffDate))
                .toList();
    }

    private void assertValidPasswordHistory(List<PasswordHistoryEntry> passwordHistory) {
        if (passwordHistory == null || passwordHistory.isEmpty()) {
            throw new StaffAccountPasswordHistoryException(StaffAccountPasswordHistoryExceptionInfo.mustContainEntries());
        }

        StaffAccountId staffAccountId = passwordHistory.getFirst().staffAccountId();
        boolean mixedAccounts = passwordHistory.stream()
                .anyMatch(entry -> !entry.staffAccountId().equals(staffAccountId));
        if (mixedAccounts) {
            throw new StaffAccountPasswordHistoryException(StaffAccountPasswordHistoryExceptionInfo.entriesMustBelongToSingleAccount());
        }
    }
}
