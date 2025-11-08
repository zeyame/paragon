package com.paragon.domain.events.staffaccountevents;

import com.paragon.domain.events.EventNames;
import com.paragon.domain.models.aggregates.StaffAccount;

public class StaffAccountEnabledEvent extends StaffAccountEventBase {
    public StaffAccountEnabledEvent(StaffAccount staffAccount) {
        super(staffAccount, EventNames.STAFF_ACCOUNT_ENABLED);
    }
}
