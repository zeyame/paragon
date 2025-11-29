package com.paragon.domain.services;

import com.paragon.application.common.interfaces.PasswordHasher;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyException;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyExceptionInfo;
import com.paragon.domain.models.valueobjects.*;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;

public final class StaffAccountPasswordReusePolicy {
    private static final Period PASSWORD_REUSE_RESTRICTION_WINDOW = Period.ofMonths(3);

    public static void ensureNotViolated(PlaintextPassword enteredPassword,
                                         StaffAccountPasswordHistory passwordHistory,
                                         PasswordHasher passwordHasher) {
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
