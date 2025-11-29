package com.paragon.integration.persistence;

import com.paragon.domain.interfaces.StaffAccountPasswordHistoryWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;
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
}
