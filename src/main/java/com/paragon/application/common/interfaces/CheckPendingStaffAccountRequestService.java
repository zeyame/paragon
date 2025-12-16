package com.paragon.application.common.interfaces;

import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.models.valueobjects.StaffAccountId;

public interface CheckPendingStaffAccountRequestService {
    void ensureNoPendingRequest(StaffAccountId staffAccountId, StaffAccountRequestType requestType);
}
