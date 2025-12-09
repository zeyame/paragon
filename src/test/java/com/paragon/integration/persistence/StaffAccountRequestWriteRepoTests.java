package com.paragon.integration.persistence;

import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.helpers.fixtures.StaffAccountRequestFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.write.StaffAccountRequestWriteRepoImpl;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountRequestWriteRepoTests {
    @Nested
    class Create extends IntegrationTestBase {
        private final StaffAccountRequestWriteRepoImpl sut;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public Create(WriteJdbcHelper writeJdbcHelper) {
            sut = new StaffAccountRequestWriteRepoImpl(writeJdbcHelper);
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldCreateNewStaffAccountRequest() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            StaffAccountRequest insertedRequest = new StaffAccountRequestFixture()
                    .withSubmittedBy(staffAccount.getId().getValue().toString())
                    .build();

            // When
            sut.create(insertedRequest);

            // Then
            Optional<StaffAccountRequest> optionalRequest = jdbcHelper.getStaffAccountRequestById(insertedRequest.getId());
            assertThat(optionalRequest).isPresent();

            StaffAccountRequest retrievedRequest = optionalRequest.get();
            assertThat(retrievedRequest)
                    .usingRecursiveComparison()
                    .isEqualTo(insertedRequest);
        }
    }
}