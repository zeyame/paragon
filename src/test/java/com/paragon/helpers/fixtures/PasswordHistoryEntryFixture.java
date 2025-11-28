package com.paragon.helpers.fixtures;

import com.paragon.domain.models.valueobjects.*;

import java.time.Instant;
import java.util.UUID;

public class PasswordHistoryEntryFixture {
    private String staffAccountId = UUID.randomUUID().toString();
    private String hashedPassword = Password.of(PlaintextPassword.generate().getValue()).getValue();
    private boolean isTemporary = false;
    private Instant changedAt = Instant.now();

    public PasswordHistoryEntryFixture withStaffAccountId(String value) {
        this.staffAccountId = value;
        return this;
    }

    public PasswordHistoryEntryFixture withHashedPassword(String value) {
        this.hashedPassword = value;
        return this;
    }

    public PasswordHistoryEntryFixture withTemporary(boolean value) {
        this.isTemporary = value;
        return this;
    }

    public PasswordHistoryEntryFixture withChangedAt(Instant value) {
        this.changedAt = value;
        return this;
    }

    public PasswordHistoryEntry build() {
        return new PasswordHistoryEntry(
                StaffAccountId.from(staffAccountId),
                Password.fromHashed(hashedPassword),
                isTemporary,
                DateTimeUtc.of(changedAt)
        );
    }

    public static PasswordHistoryEntry validEntry() {
        return new PasswordHistoryEntryFixture().build();
    }
}
