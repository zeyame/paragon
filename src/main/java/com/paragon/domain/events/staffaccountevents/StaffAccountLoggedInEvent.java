package com.paragon.domain.events.staffaccountevents;

import com.paragon.domain.events.EventNames;
import com.paragon.domain.models.aggregates.StaffAccount;

public class StaffAccountLoggedInEvent extends StaffAccountEventBase {
    public StaffAccountLoggedInEvent(StaffAccount staffAccount) {
        super(staffAccount, EventNames.STAFF_ACCOUNT_LOGGED_IN);
    }
}
