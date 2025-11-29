package com.paragon.domain.services;

import com.paragon.application.common.interfaces.PasswordHasher;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyException;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyExceptionInfo;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;
import com.paragon.domain.models.valueobjects.PlaintextPassword;
import com.paragon.domain.models.valueobjects.StaffAccountPasswordHistory;
import com.paragon.helpers.fixtures.PasswordHistoryEntryFixture;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StaffAccountPasswordReusePolicyTests {
    private final PasswordHasher passwordHasherMock;

    public StaffAccountPasswordReusePolicyTests() {
        this.passwordHasherMock = mock(PasswordHasher.class);
    }

    @Test
    void shouldNotThrow_whenEnteredPasswordWasNeverUsed() {
        // Given
        StaffAccountPasswordHistory passwordHistory = new StaffAccountPasswordHistory(List.of(newEntryWithinRestrictionWindow(), newEntryWithinRestrictionWindow()));
        when(passwordHasherMock.verify(anyString(), any(Password.class)))
                .thenReturn(false);

        // When & Then
        assertThatNoException().isThrownBy(() ->
                StaffAccountPasswordReusePolicy.ensureNotViolated(PlaintextPassword.generate(), passwordHistory, passwordHasherMock)
        );
    }

    @Test
    void shouldNotThrow_whenEnteredPasswordWasUsedPriorToRestrictionWindow() {
        // Given
        StaffAccountPasswordHistory passwordHistory = new StaffAccountPasswordHistory(
                List.of(newEntryPriorToRestrictionWindow(), newEntryPriorToRestrictionWindow())
        );

        when(passwordHasherMock.verify(anyString(), any(Password.class)))
                .thenReturn(true);

        // When & Then
        assertThatNoException().isThrownBy(() ->
                StaffAccountPasswordReusePolicy.ensureNotViolated(PlaintextPassword.generate(), passwordHistory, passwordHasherMock)
        );
    }

    @Test
    void shouldThrow_whenEnteredPasswordWasUsedWithinRestrictionWindow() {
        // Given
        StaffAccountPasswordHistory passwordHistory = new StaffAccountPasswordHistory(
                List.of(newEntryWithinRestrictionWindow(), newEntryWithinRestrictionWindow())
        );

        when(passwordHasherMock.verify(anyString(), any(Password.class)))
                .thenReturn(true);
        StaffAccountPasswordReusePolicyException expectedException = new StaffAccountPasswordReusePolicyException(
                StaffAccountPasswordReusePolicyExceptionInfo.passwordUsedWithinRestrictedWindow()
        );

        // When & Then
        assertThatExceptionOfType(StaffAccountPasswordReusePolicyException.class)
                .isThrownBy(() -> StaffAccountPasswordReusePolicy.ensureNotViolated(PlaintextPassword.generate(), passwordHistory, passwordHasherMock))
                .extracting("message", "domainErrorCode")
                .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
    }

    private static PasswordHistoryEntry newEntryWithinRestrictionWindow() {
        return new PasswordHistoryEntryFixture()
                .withChangedAt(LocalDateTime.now()
                        .minusMonths(2)
                        .toInstant(ZoneOffset.UTC))
                .build();
    }

    private static PasswordHistoryEntry newEntryPriorToRestrictionWindow() {
        Instant threeMonthsAgo = LocalDateTime.now()
                .minusMonths(4)
                .toInstant(ZoneOffset.UTC);

        return new PasswordHistoryEntryFixture()
                .withChangedAt(threeMonthsAgo)
                .build();
    }
}
