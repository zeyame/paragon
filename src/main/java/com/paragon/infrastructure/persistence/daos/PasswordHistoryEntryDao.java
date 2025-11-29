package com.paragon.infrastructure.persistence.daos;

import com.paragon.domain.models.valueobjects.DateTimeUtc;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;
import com.paragon.domain.models.valueobjects.StaffAccountId;

import java.time.Instant;
import java.util.UUID;

public record PasswordHistoryEntryDao(
        UUID id,
        UUID staffAccountId,
        String hashedPassword,
        boolean isTemporary,
        Instant changedAtUtc
) {
    public PasswordHistoryEntry toPasswordHistoryEntry() {
        return new PasswordHistoryEntry(
                StaffAccountId.of(staffAccountId),
                Password.of(hashedPassword),
                isTemporary,
                DateTimeUtc.of(changedAtUtc)
        );
    }
}
