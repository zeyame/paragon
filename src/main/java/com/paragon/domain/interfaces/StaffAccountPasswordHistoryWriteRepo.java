package com.paragon.domain.interfaces;

import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;

public interface StaffAccountPasswordHistoryWriteRepo {
    void appendEntry(PasswordHistoryEntry entry);
}
