package com.paragon.domain.interfaces;

import com.paragon.domain.models.aggregates.StaffAccountRequest;

public interface StaffAccountRequestWriteRepo {
    void create(StaffAccountRequest request);
}
