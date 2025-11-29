package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.StaffAccountPasswordHistoryException;
import com.paragon.domain.exceptions.valueobject.StaffAccountPasswordHistoryExceptionInfo;
import com.paragon.helpers.fixtures.PasswordHistoryEntryFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}
