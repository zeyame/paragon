package com.paragon.domain.interfaces;

import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.StaffAccountId;

import java.util.Optional;

public interface StaffAccountWriteRepo {
    void create(StaffAccount staffAccount);
    Optional<StaffAccount> getById(StaffAccountId staffAccountId);
}
