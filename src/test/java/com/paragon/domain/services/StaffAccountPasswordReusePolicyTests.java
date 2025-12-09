package com.paragon.domain.services;

import com.paragon.domain.interfaces.services.PasswordHasher;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyException;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyExceptionInfo;
import com.paragon.domain.interfaces.services.StaffAccountPasswordReusePolicy;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;
import com.paragon.domain.models.valueobjects.PlaintextPassword;
import com.paragon.domain.models.valueobjects.StaffAccountPasswordHistory;
import com.paragon.helpers.fixtures.PasswordHistoryEntryFixture;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StaffAccountPasswordReusePolicyTests {
    private final StaffAccountPasswordReusePolicy sut;
    private final PasswordHasher passwordHasherMock;

    public StaffAccountPasswordReusePolicyTests() {
        this.passwordHasherMock = mock(PasswordHasher.class);
        sut = new StaffAccountPasswordReusePolicyImpl(passwordHasherMock);
    }

    @Test
    void shouldNotThrow_whenEnteredPasswordWasNeverUsed() {
        // Given
        String staffAccountId = UUID.randomUUID().toString();
        StaffAccountPasswordHistory passwordHistory = new StaffAccountPasswordHistory(
                List.of(newEntryWithinRestrictionWindow(staffAccountId),
                        newEntryWithinRestrictionWindow(staffAccountId))
        );
        when(passwordHasherMock.verify(anyString(), any(Password.class)))
                .thenReturn(false);

        // When & Then
        assertThatNoException().isThrownBy(() ->
                sut.ensureNotViolated(PlaintextPassword.generate(), passwordHistory)
        );
    }

    @Test
    void shouldNotThrow_whenEnteredPasswordWasUsedPriorToRestrictionWindow() {
        // Given
        String staffAccountId = UUID.randomUUID().toString();
        StaffAccountPasswordHistory passwordHistory = new StaffAccountPasswordHistory(
                List.of(newEntryPriorToRestrictionWindow(staffAccountId), newEntryPriorToRestrictionWindow(staffAccountId))
        );

        when(passwordHasherMock.verify(anyString(), any(Password.class)))
                .thenReturn(true);

        // When & Then
        assertThatNoException().isThrownBy(() ->
                sut.ensureNotViolated(PlaintextPassword.generate(), passwordHistory)
        );
    }

    @Test
    void shouldThrow_whenEnteredPasswordWasUsedWithinRestrictionWindow() {
        // Given
        String staffAccountId = UUID.randomUUID().toString();
        StaffAccountPasswordHistory passwordHistory = new StaffAccountPasswordHistory(
                List.of(newEntryWithinRestrictionWindow(staffAccountId), newEntryWithinRestrictionWindow(staffAccountId))
        );

        when(passwordHasherMock.verify(anyString(), any(Password.class)))
                .thenReturn(true);
        StaffAccountPasswordReusePolicyException expectedException = new StaffAccountPasswordReusePolicyException(
                StaffAccountPasswordReusePolicyExceptionInfo.passwordUsedWithinRestrictedWindow()
        );

        // When & Then
        assertThatExceptionOfType(StaffAccountPasswordReusePolicyException.class)
                .isThrownBy(() -> sut.ensureNotViolated(PlaintextPassword.generate(), passwordHistory))
                .extracting("message", "domainErrorCode")
                .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
    }

    private static PasswordHistoryEntry newEntryWithinRestrictionWindow(String staffAccountId) {
        Instant threeMonthsAgo = LocalDateTime.now()
                .minusMonths(3)
                .toInstant(ZoneOffset.UTC);
        return new PasswordHistoryEntryFixture()
                .withStaffAccountId(staffAccountId)
                .withChangedAt(threeMonthsAgo)
                .build();
    }

    private static PasswordHistoryEntry newEntryPriorToRestrictionWindow(String staffAccountId) {
        Instant fourMonthsAgo = LocalDateTime.now()
                .minusMonths(4)
                .toInstant(ZoneOffset.UTC);

        return new PasswordHistoryEntryFixture()
                .withStaffAccountId(staffAccountId)
                .withChangedAt(fourMonthsAgo)
                .build();
    }
}
