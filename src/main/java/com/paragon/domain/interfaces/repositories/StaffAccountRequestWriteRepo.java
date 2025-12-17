package com.paragon.domain.interfaces.repositories;

import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.domain.models.valueobjects.StaffAccountId;

public interface StaffAccountRequestWriteRepo {
    void create(StaffAccountRequest request);
    boolean existsPendingRequestBySubmitterAndType(StaffAccountId submitter, StaffAccountRequestType requestType);
}
