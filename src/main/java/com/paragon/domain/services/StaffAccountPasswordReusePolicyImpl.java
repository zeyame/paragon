package com.paragon.domain.services;

import com.paragon.application.common.interfaces.PasswordHasher;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyException;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyExceptionInfo;
import com.paragon.domain.interfaces.StaffAccountPasswordReusePolicy;
import com.paragon.domain.models.valueobjects.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;

@Service
public final class StaffAccountPasswordReusePolicyImpl implements StaffAccountPasswordReusePolicy {
    private static final Period PASSWORD_REUSE_RESTRICTION_WINDOW = Period.ofMonths(3);
    private final PasswordHasher passwordHasher;

    public StaffAccountPasswordReusePolicyImpl(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void ensureNotViolated(PlaintextPassword enteredPassword, StaffAccountPasswordHistory passwordHistory) {
        DateTimeUtc cutOffDate = DateTimeUtc.of(
                Instant.now()
                        .atZone(ZoneOffset.UTC)
                        .minus(PASSWORD_REUSE_RESTRICTION_WINDOW)
                        .toInstant()
        );
        List<PasswordHistoryEntry> filteredEntries = passwordHistory.entriesOnOrAfter(cutOffDate);
        for (PasswordHistoryEntry entry : filteredEntries) {
            throwIfPasswordsAreEqual(enteredPassword, entry.hashedPassword(), passwordHasher);
        }
    }

    private static void throwIfPasswordsAreEqual(PlaintextPassword enteredPassword, Password hashedPassword, PasswordHasher passwordHasher) {
        if (passwordHasher.verify(enteredPassword.getValue(), hashedPassword)) {
            throw new StaffAccountPasswordReusePolicyException(StaffAccountPasswordReusePolicyExceptionInfo.passwordUsedWithinRestrictedWindow());
        }
    }
}
