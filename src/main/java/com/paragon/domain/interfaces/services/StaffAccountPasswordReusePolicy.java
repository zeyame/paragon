package com.paragon.domain.interfaces.services;

import com.paragon.domain.models.valueobjects.PlaintextPassword;
import com.paragon.domain.models.valueobjects.StaffAccountPasswordHistory;

public interface StaffAccountPasswordReusePolicy {
    void ensureNotViolated(PlaintextPassword enteredPassword, StaffAccountPasswordHistory passwordHistory);
}
