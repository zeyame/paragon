package com.paragon.domain.events.staffaccountevents;

import com.paragon.domain.events.EventNames;
import com.paragon.domain.models.aggregates.StaffAccount;

public class StaffAccountRegisteredEvent extends StaffAccountEventBase {
    public StaffAccountRegisteredEvent(StaffAccount staffAccount) {
        super(staffAccount, EventNames.STAFF_ACCOUNT_REGISTERED);
    }
}
