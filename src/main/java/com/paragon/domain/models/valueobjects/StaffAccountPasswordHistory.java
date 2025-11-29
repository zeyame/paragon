package com.paragon.domain.models.valueobjects;

import java.util.List;

public record StaffAccountPasswordHistory(List<PasswordHistoryEntry> passwordHistory) {
    public StaffAccountPasswordHistory(List<PasswordHistoryEntry> passwordHistory) {
        this.passwordHistory = passwordHistory;
    }

    public List<PasswordHistoryEntry> entriesOnOrAfter(DateTimeUtc cutOffDate) {
        return passwordHistory.stream()
                .filter(entry -> entry.changedAt().isAfter(cutOffDate))
                .toList();
    }
}
