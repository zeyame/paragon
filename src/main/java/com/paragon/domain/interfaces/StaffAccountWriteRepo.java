package com.paragon.domain.interfaces;

import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;

import java.util.Optional;

public interface StaffAccountWriteRepo {
    void create(StaffAccount staffAccount);
    Optional<StaffAccount> getById(StaffAccountId staffAccountId);
    Optional<StaffAccount> getByUsername(Username username);
}
