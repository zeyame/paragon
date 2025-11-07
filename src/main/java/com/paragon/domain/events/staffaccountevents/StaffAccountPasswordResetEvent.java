package com.paragon.domain.events.staffaccountevents;

import com.paragon.domain.events.EventNames;
import com.paragon.domain.models.aggregates.StaffAccount;

public class StaffAccountPasswordResetEvent extends StaffAccountEventBase {
    public StaffAccountPasswordResetEvent(StaffAccount staffAccount) {
        super(staffAccount, EventNames.STAFF_ACCOUNT_PASSWORD_RESET);
    }
}
