package com.paragon.domain.interfaces.repositories;

import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.StaffAccountPasswordHistory;

public interface StaffAccountPasswordHistoryWriteRepo {
    void appendEntry(PasswordHistoryEntry entry);
    StaffAccountPasswordHistory getPasswordHistory(StaffAccountId staffAccountId);
}
