package com.paragon.integration.persistence;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountRequestReadRepo;
import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.helpers.fixtures.StaffAccountRequestFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountRequestReadModel;
import com.paragon.infrastructure.persistence.repos.read.StaffAccountRequestReadRepoImpl;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountRequestReadRepoTests {
    @Nested
    class GetPendingRequestBySubmitterAndType extends IntegrationTestBase {
        private final StaffAccountRequestReadRepo sut;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public GetPendingRequestBySubmitterAndType(ReadJdbcHelper readJdbcHelper, WriteJdbcHelper writeJdbcHelper) {
            sut = new StaffAccountRequestReadRepoImpl(readJdbcHelper);
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnPendingRequestWithJoinedUsername_whenOneExists() {
            // Given
            StaffAccount submitter = new StaffAccountFixture()
                    .withUsername("john_doe")
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(submitter);

            StaffAccountRequest request = new StaffAccountRequestFixture()
                    .withSubmittedBy(submitter.getId().getValue().toString())
                    .withRequestType(StaffAccountRequestType.PASSWORD_CHANGE)
                    .withStatus(StaffAccountRequestStatus.PENDING)
                    .build();
            jdbcHelper.insertStaffAccountRequest(request);

            // When
            Optional<StaffAccountRequestReadModel> result = sut.getPendingRequestBySubmitterAndType(
                    submitter.getId(),
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(result).isPresent();
            StaffAccountRequestReadModel readModel = result.get();
            assertThat(readModel.id()).isEqualTo(request.getId().getValue());
            assertThat(readModel.submittedBy()).isEqualTo(submitter.getId().getValue());
            assertThat(readModel.submittedByUsername()).isEqualTo("john_doe");
            assertThat(readModel.requestType()).isEqualTo(StaffAccountRequestType.PASSWORD_CHANGE);
            assertThat(readModel.status()).isEqualTo(StaffAccountRequestStatus.PENDING);
            assertThat(readModel.targetId()).isNull();
            assertThat(readModel.targetType()).isNull();
            assertThat(readModel.approvedBy()).isNull();
            assertThat(readModel.approvedByUsername()).isNull();
            assertThat(readModel.approvedAtUtc()).isNull();
            assertThat(readModel.rejectedBy()).isNull();
            assertThat(readModel.rejectedByUsername()).isNull();
            assertThat(readModel.rejectedAtUtc()).isNull();
        }

        @Test
        void shouldFilterByStatus_returningEmptyForNonPendingRequests() {
            // Given
            StaffAccount submitter = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(submitter);

            // Insert
            StaffAccountRequest approvedRequest = new StaffAccountRequestFixture()
                    .withSubmittedBy(submitter.getId().getValue().toString())
                    .withRequestType(StaffAccountRequestType.PASSWORD_CHANGE)
                    .withStatus(StaffAccountRequestStatus.APPROVED)
                    .build();
            jdbcHelper.insertStaffAccountRequest(approvedRequest);

            // When
            Optional<StaffAccountRequestReadModel> result = sut.getPendingRequestBySubmitterAndType(
                    submitter.getId(),
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldFilterByRequestType_returningEmptyForDifferentType() {
            // Given
            StaffAccount submitter = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(submitter);

            StaffAccountRequest request = new StaffAccountRequestFixture()
                    .withSubmittedBy(submitter.getId().getValue().toString())
                    .withRequestType(StaffAccountRequestType.MODMAIL_TRANSCRIPT_ACCESS)
                    .withStatus(StaffAccountRequestStatus.PENDING)
                    .build();
            jdbcHelper.insertStaffAccountRequest(request);

            // When
            Optional<StaffAccountRequestReadModel> result = sut.getPendingRequestBySubmitterAndType(
                    submitter.getId(),
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(result).isEmpty();
        }
    }
}