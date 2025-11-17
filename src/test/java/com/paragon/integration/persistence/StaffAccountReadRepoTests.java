package com.paragon.integration.persistence;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;
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
import java.util.Optional;
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
    class FindByUsername extends IntegrationTestBase {
        private final StaffAccountReadRepo sut;
        private final TestJdbcHelper testJdbcHelper;

        @Autowired
        public FindByUsername(WriteJdbcHelper writeJdbcHelper, StaffAccountReadRepo staffAccountReadRepo) {
            sut = staffAccountReadRepo;
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnStaffAccountSummary_whenStaffAccountExists() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withUsername("john_doe")
                    .withEmail("john_doe@example.com")
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount);

            // When
            Optional<StaffAccountSummaryReadModel> optionalStaffAccountSummary = sut.findByUsername(Username.of("john_doe"));

            // Then
            assertThat(optionalStaffAccountSummary).isPresent();
            StaffAccountSummaryReadModel retrievedSummary = optionalStaffAccountSummary.get();
            assertThat(retrievedSummary.id()).isEqualTo(staffAccount.getId().getValue());
            assertThat(retrievedSummary.username()).isEqualTo(staffAccount.getUsername().getValue());
            assertThat(retrievedSummary.status()).isEqualTo(staffAccount.getStatus().toString());
            assertThat(retrievedSummary.orderAccessDuration()).isEqualTo(staffAccount.getOrderAccessDuration().getValueInDays());
            assertThat(retrievedSummary.modmailTranscriptAccessDuration()).isEqualTo(staffAccount.getModmailTranscriptAccessDuration().getValueInDays());
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