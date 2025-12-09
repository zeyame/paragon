package com.paragon.domain.interfaces.repositories;

import com.paragon.domain.models.aggregates.StaffAccountRequest;

public interface StaffAccountRequestWriteRepo {
    void create(StaffAccountRequest request);
}
