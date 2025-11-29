package com.paragon.integration.persistence;

import com.paragon.domain.interfaces.StaffAccountPasswordHistoryWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;
import com.paragon.domain.models.valueobjects.StaffAccountPasswordHistory;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.PasswordHistoryEntryFixture;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.StaffAccountPasswordHistoryWriteRepoImpl;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountPasswordHistoryWriteRepoTests {
    @Nested
    class AppendEntry extends IntegrationTestBase {
        private final StaffAccountPasswordHistoryWriteRepo sut;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public AppendEntry(WriteJdbcHelper writeJdbcHelper) {
            sut = new StaffAccountPasswordHistoryWriteRepoImpl(writeJdbcHelper);
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldAppendNewEntry() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            PasswordHistoryEntry appendedEntry = new PasswordHistoryEntryFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .build();

            // When
            sut.appendEntry(appendedEntry);

            // Then
            Optional<PasswordHistoryEntry> optionalEntry = jdbcHelper.getPasswordHistoryEntryByHashedPassword(appendedEntry.hashedPassword());
            assertThat(optionalEntry).isPresent();
            assertThat(optionalEntry.get()).isEqualTo(appendedEntry);
        }
    }

    @Nested
    class GetPasswordHistory extends IntegrationTestBase {
        private final StaffAccountPasswordHistoryWriteRepo sut;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public GetPasswordHistory(WriteJdbcHelper writeJdbcHelper) {
            sut = new StaffAccountPasswordHistoryWriteRepoImpl(writeJdbcHelper);
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldGetPasswordHistory() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            PasswordHistoryEntry entry1 = new PasswordHistoryEntryFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .build();
            PasswordHistoryEntry entry2 = new PasswordHistoryEntryFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .build();
            jdbcHelper.insertPasswordHistoryEntry(entry1);
            jdbcHelper.insertPasswordHistoryEntry(entry2);

            // When
            StaffAccountPasswordHistory passwordHistory = sut.getPasswordHistory(staffAccount.getId());

            // Then
            assertThat(passwordHistory.entries()).hasSize(2);
            assertThat(passwordHistory.entries()).containsExactlyInAnyOrder(entry1, entry2);
        }
    }
}
