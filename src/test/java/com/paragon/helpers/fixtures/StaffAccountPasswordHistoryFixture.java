package com.paragon.helpers.fixtures;

import com.paragon.domain.models.valueobjects.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class StaffAccountPasswordHistoryFixture {
    private List<PasswordHistoryEntry> entries = new ArrayList<>();
    private StaffAccountId staffAccountId = StaffAccountId.generate();

    public StaffAccountPasswordHistoryFixture withStaffAccountId(StaffAccountId value) {
        this.staffAccountId = value;
        return this;
    }

    public StaffAccountPasswordHistoryFixture withEntry(PasswordHistoryEntry entry) {
        this.entries.add(entry);
        return this;
    }

    public StaffAccountPasswordHistoryFixture withEntries(List<PasswordHistoryEntry> entries) {
        this.entries.addAll(entries);
        return this;
    }

    public StaffAccountPasswordHistory build() {
        if (entries.isEmpty()) {
            // Add a default entry if none provided
            entries.add(createDefaultEntry(staffAccountId));
        }
        return new StaffAccountPasswordHistory(entries);
    }

    public static StaffAccountPasswordHistory validHistory() {
        StaffAccountId staffAccountId = StaffAccountId.generate();

        List<PasswordHistoryEntry> entries = List.of(
            createEntryForStaffAccount(staffAccountId, 90),  // 3 months ago
            createEntryForStaffAccount(staffAccountId, 60),  // 2 months ago
            createEntryForStaffAccount(staffAccountId, 30)   // 1 month ago
        );

        return new StaffAccountPasswordHistory(entries);
    }

    public static StaffAccountPasswordHistory validHistoryForStaffAccount(StaffAccountId staffAccountId) {
        List<PasswordHistoryEntry> entries = List.of(
            createEntryForStaffAccount(staffAccountId, 90),
            createEntryForStaffAccount(staffAccountId, 60),
            createEntryForStaffAccount(staffAccountId, 30)
        );

        return new StaffAccountPasswordHistory(entries);
    }

    public static StaffAccountPasswordHistory emptyHistoryForStaffAccount(StaffAccountId staffAccountId) {
        return new StaffAccountPasswordHistory(List.of(
            createDefaultEntry(staffAccountId)
        ));
    }

    private static PasswordHistoryEntry createEntryForStaffAccount(StaffAccountId staffAccountId, int daysAgo) {
        return new PasswordHistoryEntry(
            staffAccountId,
            Password.of(PlaintextPassword.generate().getValue()),
            false,
            DateTimeUtc.of(Instant.now().minus(daysAgo, ChronoUnit.DAYS))
        );
    }

    private static PasswordHistoryEntry createDefaultEntry(StaffAccountId staffAccountId) {
        return new PasswordHistoryEntry(
            staffAccountId,
            Password.of(PlaintextPassword.generate().getValue()),
            false,
            DateTimeUtc.now()
        );
    }
}