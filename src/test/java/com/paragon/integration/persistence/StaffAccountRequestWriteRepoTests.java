package com.paragon.integration.persistence;

import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.enums.TargetType;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.helpers.fixtures.StaffAccountRequestFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.write.StaffAccountRequestWriteRepoImpl;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
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

    @Nested
    class ExistsPendingRequestBySubmitterAndType extends IntegrationTestBase {
        private final StaffAccountRequestWriteRepoImpl sut;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public ExistsPendingRequestBySubmitterAndType(WriteJdbcHelper writeJdbcHelper) {
            sut = new StaffAccountRequestWriteRepoImpl(writeJdbcHelper);
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnTrue_whenPendingPasswordChangeRequestExists() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            StaffAccountRequest pendingRequest = new StaffAccountRequestFixture()
                    .withSubmittedBy(staffAccount.getId().getValue().toString())
                    .withRequestType(StaffAccountRequestType.PASSWORD_CHANGE)
                    .withStatus(StaffAccountRequestStatus.PENDING)
                    .build();
            jdbcHelper.insertStaffAccountRequest(pendingRequest);

            // When
            boolean result = sut.existsPendingRequestBySubmitterAndType(
                    staffAccount.getId(),
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalse_whenNoPendingRequestExists() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            // When
            boolean result = sut.existsPendingRequestBySubmitterAndType(
                    staffAccount.getId(),
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenRequestExistsButIsApproved() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            StaffAccountRequest approvedRequest = new StaffAccountRequestFixture()
                    .withSubmittedBy(staffAccount.getId().getValue().toString())
                    .withRequestType(StaffAccountRequestType.PASSWORD_CHANGE)
                    .withStatus(StaffAccountRequestStatus.APPROVED)
                    .withApprovedBy(adminId)
                    .withApprovedAt(Instant.now())
                    .build();
            jdbcHelper.insertStaffAccountRequest(approvedRequest);

            // When
            boolean result = sut.existsPendingRequestBySubmitterAndType(
                    staffAccount.getId(),
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenRequestExistsButIsRejected() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            StaffAccountRequest rejectedRequest = new StaffAccountRequestFixture()
                    .withSubmittedBy(staffAccount.getId().getValue().toString())
                    .withRequestType(StaffAccountRequestType.PASSWORD_CHANGE)
                    .withStatus(StaffAccountRequestStatus.REJECTED)
                    .withRejectedBy(adminId)
                    .withRejectedAt(Instant.now())
                    .build();
            jdbcHelper.insertStaffAccountRequest(rejectedRequest);

            // When
            boolean result = sut.existsPendingRequestBySubmitterAndType(
                    staffAccount.getId(),
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenPendingRequestExistsButForDifferentType() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            StaffAccountRequest orderAccessRequest = new StaffAccountRequestFixture()
                    .withSubmittedBy(staffAccount.getId().getValue().toString())
                    .withRequestType(StaffAccountRequestType.CENSORED_ORDER_CONTENT)
                    .withStatus(StaffAccountRequestStatus.PENDING)
                    .withTargetId("order-123")
                    .withTargetType(TargetType.ORDER)
                    .build();
            jdbcHelper.insertStaffAccountRequest(orderAccessRequest);

            // When
            boolean result = sut.existsPendingRequestBySubmitterAndType(
                    staffAccount.getId(),
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenPendingRequestExistsButForDifferentSubmitter() {
            // Given
            StaffAccount staffAccount1 = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount1);

            StaffAccount staffAccount2 = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount2);

            StaffAccountRequest pendingRequest = new StaffAccountRequestFixture()
                    .withSubmittedBy(staffAccount1.getId().getValue().toString())
                    .withRequestType(StaffAccountRequestType.PASSWORD_CHANGE)
                    .withStatus(StaffAccountRequestStatus.PENDING)
                    .build();
            jdbcHelper.insertStaffAccountRequest(pendingRequest);

            // When
            boolean result = sut.existsPendingRequestBySubmitterAndType(
                    staffAccount2.getId(),
                    StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(result).isFalse();
        }
    }
}