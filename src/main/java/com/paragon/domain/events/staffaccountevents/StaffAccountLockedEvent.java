package com.paragon.domain.events.staffaccountevents;

import com.paragon.domain.events.EventNames;
import com.paragon.domain.models.aggregates.StaffAccount;

public class StaffAccountLockedEvent extends StaffAccountEventBase {
    public StaffAccountLockedEvent(StaffAccount staffAccount) {
        super(staffAccount, EventNames.STAFF_ACCOUNT_LOCKED);
    }
}
