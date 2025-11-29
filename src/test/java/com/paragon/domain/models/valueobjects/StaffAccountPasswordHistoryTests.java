package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.StaffAccountPasswordHistoryException;
import com.paragon.domain.exceptions.valueobject.StaffAccountPasswordHistoryExceptionInfo;
import com.paragon.helpers.fixtures.PasswordHistoryEntryFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class StaffAccountPasswordHistoryTests {
    @Nested
    class Constructor {
        @Test
        void shouldCreateUnmodifiableCopyOfEntries() {
            // Given
            UUID staffAccountId = UUID.randomUUID();
            List<PasswordHistoryEntry> validEntries = new ArrayList<>(List.of(
                    new PasswordHistoryEntryFixture().withStaffAccountId(staffAccountId.toString()).build(),
                    new PasswordHistoryEntryFixture().withStaffAccountId(staffAccountId.toString()).build()
            ));

            // When
            StaffAccountPasswordHistory passwordHistory = new StaffAccountPasswordHistory(validEntries);

            // Then
            assertThat(validEntries == passwordHistory.entries()).isFalse();
            assertThatException().isThrownBy(() ->
                    passwordHistory.entries().add(PasswordHistoryEntryFixture.validEntry())
            );

            validEntries.add(new PasswordHistoryEntryFixture().withStaffAccountId(staffAccountId.toString()).build());
            assertThat(passwordHistory.entries()).hasSize(2);
        }

        @Test
        void shouldNotThrow_whenPassedEntriesAreValid() {
            // Given
            UUID staffAccountId = UUID.randomUUID();
            List<PasswordHistoryEntry> validEntries = List.of(
                    new PasswordHistoryEntryFixture().withStaffAccountId(staffAccountId.toString()).build(),
                    new PasswordHistoryEntryFixture().withStaffAccountId(staffAccountId.toString()).build()
            );

            // When & Then
            assertThatNoException()
                    .isThrownBy(() -> new StaffAccountPasswordHistory(validEntries));
        }

        @Test
        void shouldThrow_whenEntriesAreNull() {
            // Given
            StaffAccountPasswordHistoryException expectedException = new StaffAccountPasswordHistoryException(
                    StaffAccountPasswordHistoryExceptionInfo.mustContainEntries()
            );

            // When & Then
            assertThatExceptionOfType(StaffAccountPasswordHistoryException.class)
                    .isThrownBy(() -> new StaffAccountPasswordHistory(null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
        }

        @Test
        void shouldThrow_whenEntriesAreEmpty() {
            // Given
            StaffAccountPasswordHistoryException expectedException = new StaffAccountPasswordHistoryException(
                    StaffAccountPasswordHistoryExceptionInfo.mustContainEntries()
            );

            // When & Then
            assertThatExceptionOfType(StaffAccountPasswordHistoryException.class)
                    .isThrownBy(() -> new StaffAccountPasswordHistory(List.of()))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
        }

        @Test
        void shouldThrow_whenEntriesDoNotBelongToTheSameStaffAccount() {
            // Given
            List<PasswordHistoryEntry> mixedEntries = List.of(PasswordHistoryEntryFixture.validEntry(), PasswordHistoryEntryFixture.validEntry());

            StaffAccountPasswordHistoryException expectedException = new StaffAccountPasswordHistoryException(
                    StaffAccountPasswordHistoryExceptionInfo.entriesMustBelongToSingleAccount()
            );

            // When & Then
            assertThatExceptionOfType(StaffAccountPasswordHistoryException.class)
                    .isThrownBy(() -> new StaffAccountPasswordHistory(mixedEntries))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
        }
    }

    @Nested
    class EntriesOnOrAfter {
        @Test
        void shouldReturnEntriesOnOrAfterCutOffDate() {
            // Given
            UUID staffAccountId = UUID.randomUUID();
            List<PasswordHistoryEntry> validEntries = List.of(
                    newEntryWithinRestrictionWindow(staffAccountId.toString()),
                    newEntryWithinRestrictionWindow(staffAccountId.toString()),
                    newEntryPriorToRestrictionWindow(staffAccountId.toString())
            );
            StaffAccountPasswordHistory passwordHistory = new StaffAccountPasswordHistory(validEntries);
            DateTimeUtc cutOffDate = DateTimeUtc.of(
                    Instant.now()
                            .atZone(ZoneOffset.UTC)
                            .minus(Period.ofMonths(3))
                            .toInstant()
            );

            // When
            List<PasswordHistoryEntry> filteredEntries = passwordHistory.entriesOnOrAfter(cutOffDate);

            // Then
            assertThat(filteredEntries).hasSize(2);
            assertThat(filteredEntries.contains(validEntries.get(2))).isFalse(); // entry prior to cut off date is not returned
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
}
