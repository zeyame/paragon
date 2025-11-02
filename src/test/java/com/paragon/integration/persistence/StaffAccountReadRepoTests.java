package com.paragon.integration.persistence;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class StaffAccountReadRepoTests {
    @Nested
    class Exists extends IntegrationTestBase {
        private final StaffAccountReadRepo sut;
        private final TestJdbcHelper testJdbcHelper;

        @Autowired
        public Exists(WriteJdbcHelper writeJdbcHelper, StaffAccountReadRepo staffAccountReadRepo) {
            sut = staffAccountReadRepo;
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnTrue_whenStaffAccountExists() {
            // Given
            StaffAccount insertedStaffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(insertedStaffAccount);

            // When
            boolean exists = sut.exists(insertedStaffAccount.getId());

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        void shouldReturnFalse_whenStaffAccountDoesNotExist() {
            // Given
            StaffAccountId nonExistentId = StaffAccountId.generate();

            // When
            boolean exists = sut.exists(nonExistentId);

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    class HasPermission extends IntegrationTestBase {
        private final StaffAccountReadRepo sut;
        private final TestJdbcHelper testJdbcHelper;

        @Autowired
        public HasPermission(WriteJdbcHelper writeJdbcHelper, StaffAccountReadRepo staffAccountReadRepo) {
            sut = staffAccountReadRepo;
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnTrue_whenStaffAccountHasPermission() {
            // Given
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");
            StaffAccount insertedStaffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of(permissionCode.getValue(), "VIEW_LOGIN_LOGS"))
                    .build();
            testJdbcHelper.insertStaffAccount(insertedStaffAccount);

            // When
            boolean hasPermission = sut.hasPermission(insertedStaffAccount.getId(), permissionCode);

            // Then
            assertThat(hasPermission).isTrue();
        }

        @Test
        void shouldReturnFalse_whenStaffAccountDoesNotHavePermission() {
            // Given
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");
            StaffAccount insertedStaffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of("VIEW_LOGIN_LOGS"))
                    .build();
            testJdbcHelper.insertStaffAccount(insertedStaffAccount);

            // When
            boolean hasPermission = sut.hasPermission(insertedStaffAccount.getId(), permissionCode);

            // Then
            assertThat(hasPermission).isFalse();
        }

        @Test
        void shouldReturnFalse_whenStaffAccountDoesNotExist() {
            // Given
            StaffAccountId nonExistentId = StaffAccountId.generate();
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");

            // When
            boolean hasPermission = sut.hasPermission(nonExistentId, permissionCode);

            // Then
            assertThat(hasPermission).isFalse();
        }
    }

    @Nested
    class FindAllSummaries extends IntegrationTestBase {
        private final StaffAccountReadRepo sut;
        private final TestJdbcHelper testJdbcHelper;

        @Autowired
        public FindAllSummaries(WriteJdbcHelper writeJdbcHelper, StaffAccountReadRepo staffAccountReadRepo) {
            sut = staffAccountReadRepo;
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnAllStaffAccountSummaries() {
            // Given
            StaffAccount staffAccount1 = new StaffAccountFixture()
                    .withUsername("john_doe")
                    .withEmail("john_doe@example.com")
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount staffAccount2 = new StaffAccountFixture()
                    .withUsername("jane_smith")
                    .withEmail("jane_smith@example.com")
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount1);
            testJdbcHelper.insertStaffAccount(staffAccount2);

            // When
            List<StaffAccountSummaryReadModel> summaries = sut.findAllSummaries();

            // Then
            assertThat(summaries).isNotNull();
            assertThat(summaries.size()).isEqualTo(3); // admin + 2 created accounts

            assertThat(summaries).anySatisfy(summary -> {
                assertThat(summary.id()).isEqualTo(staffAccount1.getId().getValue());
                assertThat(summary.username()).isEqualTo(staffAccount1.getUsername().getValue());
                assertThat(summary.status()).isEqualTo(staffAccount1.getStatus().toString());
                assertThat(summary.orderAccessDuration()).isEqualTo(staffAccount1.getOrderAccessDuration().getValueInDays());
                assertThat(summary.modmailTranscriptAccessDuration()).isEqualTo(staffAccount1.getModmailTranscriptAccessDuration().getValueInDays());
            });

            assertThat(summaries).anySatisfy(summary -> {
                assertThat(summary.id()).isEqualTo(staffAccount2.getId().getValue());
                assertThat(summary.username()).isEqualTo(staffAccount2.getUsername().getValue());
                assertThat(summary.status()).isEqualTo(staffAccount2.getStatus().toString());
                assertThat(summary.orderAccessDuration()).isEqualTo(staffAccount2.getOrderAccessDuration().getValueInDays());
                assertThat(summary.modmailTranscriptAccessDuration()).isEqualTo(staffAccount2.getModmailTranscriptAccessDuration().getValueInDays());
            });

            assertThat(summaries).anySatisfy(summary -> {
                assertThat(summary.id()).isEqualTo(UUID.fromString(adminId));
                assertThat(summary.username()).isEqualTo("admin");
            });
        }

        @Test
        void shouldReturnSummariesOrderedByCreatedAtDesc() {
            // Given
            StaffAccount staffAccount1 = new StaffAccountFixture()
                    .withUsername("first_user")
                    .withEmail("first@example.com")
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount1);

            // a small delay to ensure different created at timestamps
            delay();

            StaffAccount staffAccount2 = new StaffAccountFixture()
                    .withUsername("second_user")
                    .withEmail("second@example.com")
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount2);

            // When
            List<StaffAccountSummaryReadModel> summaries = sut.findAllSummaries();

            // Then
            assertThat(summaries).isNotNull();

            // the most recently created should be first (DESC order)
            assertThat(summaries.getFirst().username()).isEqualTo("second_user");
        }

        @Test
        void shouldReturnOnlyAdminAccount_whenNoOtherAccountsExist() {
            // When
            List<StaffAccountSummaryReadModel> summaries = sut.findAllSummaries();

            // Then
            assertThat(summaries).isNotNull();
            assertThat(summaries.size()).isEqualTo(1);
            assertThat(summaries.getFirst().id()).isEqualTo(UUID.fromString(adminId));
            assertThat(summaries.getFirst().username()).isEqualTo("admin");
        }
    }

    private static void delay() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}