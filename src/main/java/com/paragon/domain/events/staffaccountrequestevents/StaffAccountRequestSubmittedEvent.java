package com.paragon.domain.events.staffaccountrequestevents;

import com.paragon.domain.events.EventNames;
import com.paragon.domain.models.aggregates.StaffAccountRequest;

public class StaffAccountRequestSubmittedEvent extends StaffAccountRequestEventBase {
    public StaffAccountRequestSubmittedEvent(StaffAccountRequest staffAccountRequest) {
        super(staffAccountRequest, EventNames.STAFF_ACCOUNT_REQUEST_SUBMITTED);
    }
}
