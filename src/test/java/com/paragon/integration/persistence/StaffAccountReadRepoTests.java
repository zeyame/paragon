package com.paragon.integration.persistence;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.DateTimeUtc;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountDetailedReadModel;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
            boolean exists = sut.exists(insertedStaffAccount.getId().getValue());

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        void shouldReturnFalse_whenStaffAccountDoesNotExist() {
            // Given
            StaffAccountId nonExistentId = StaffAccountId.generate();

            // When
            boolean exists = sut.exists(nonExistentId.getValue());

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
            boolean hasPermission = sut.hasPermission(insertedStaffAccount.getId().getValue(), permissionCode);

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
            boolean hasPermission = sut.hasPermission(insertedStaffAccount.getId().getValue(), permissionCode);

            // Then
            assertThat(hasPermission).isFalse();
        }

        @Test
        void shouldReturnFalse_whenStaffAccountDoesNotExist() {
            // Given
            StaffAccountId nonExistentId = StaffAccountId.generate();
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");

            // When
            boolean hasPermission = sut.hasPermission(nonExistentId.getValue(), permissionCode);

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
            Optional<StaffAccountSummaryReadModel> optionalStaffAccountSummary = sut.findSummaryByUsername("john_doe");

            // Then
            assertThat(optionalStaffAccountSummary).isPresent();
            StaffAccountSummaryReadModel retrievedSummary = optionalStaffAccountSummary.get();
            assertThat(retrievedSummary.id()).isEqualTo(staffAccount.getId().getValue());
            assertThat(retrievedSummary.username()).isEqualTo(staffAccount.getUsername().getValue());
            assertThat(retrievedSummary.status()).isEqualTo(staffAccount.getStatus().toString());
        }
    }

    @Nested
    class FindAll extends IntegrationTestBase {
        private final StaffAccountReadRepo sut;
        private final TestJdbcHelper testJdbcHelper;

        @Autowired
        public FindAll(WriteJdbcHelper writeJdbcHelper, StaffAccountReadRepo staffAccountReadRepo) {
            sut = staffAccountReadRepo;
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnStaffAccount_whenNoFiltersProvided() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withUsername("test_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(null, null, null, null, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(staffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnActiveStaffAccount_whenStatusFilterIsActive() {
            // Given
            StaffAccount activeStaffAccount = new StaffAccountFixture()
                    .withUsername("active_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(activeStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.ACTIVE, null, null, null, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(activeStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnDisabledStaffAccount_whenStatusFilterIsDisabled() {
            // Given
            StaffAccount disabledStaffAccount = new StaffAccountFixture()
                    .withUsername("disabled_user")
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withCreatedBy(adminId)
                    .withDisabledBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(disabledStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.DISABLED, null, null, null, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(disabledStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnStaffAccountEnabledBySpecificUser_whenEnabledByFilterProvided() {
            // Given
            StaffAccountId enablerAccountId = StaffAccountId.generate();
            StaffAccount enablerAccount = new StaffAccountFixture()
                    .withId(enablerAccountId.getValue().toString())
                    .withUsername("enabler")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount enabledStaffAccount = new StaffAccountFixture()
                    .withUsername("enabled_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .withEnabledBy(enablerAccountId.getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(enablerAccount);
            testJdbcHelper.insertStaffAccount(enabledStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(null, Username.of("enabler"), null, null, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(enabledStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnStaffAccountDisabledBySpecificUser_whenDisabledByFilterProvided() {
            // Given
            StaffAccountId disablerAccountId = StaffAccountId.generate();
            StaffAccount disablerAccount = new StaffAccountFixture()
                    .withId(disablerAccountId.getValue().toString())
                    .withUsername("disabler")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount disabledStaffAccount = new StaffAccountFixture()
                    .withUsername("disabled_user")
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withCreatedBy(adminId)
                    .withDisabledBy(disablerAccountId.getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(disablerAccount);
            testJdbcHelper.insertStaffAccount(disabledStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(null, null, Username.of("disabler"), null, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(disabledStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnStaffAccountCreatedBeforeSpecificTime_whenCreatedBeforeFilterProvided() {
            // Given
            Instant beforeTime = Instant.parse("2040-06-15T12:00:00Z");
            DateTimeUtc beforeDateTime = DateTimeUtc.of(beforeTime);
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withUsername("test_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(null, null, null, beforeDateTime, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(staffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnStaffAccountCreatedAfterSpecificTime_whenCreatedAfterFilterProvided() {
            // Given
            Instant afterTime = Instant.parse("2024-11-10T12:00:00Z");
            DateTimeUtc afterDateTime = DateTimeUtc.of(afterTime);
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withUsername("test_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(null, null, null, null, afterDateTime);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(staffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnActiveStaffAccountEnabledBySpecificUser_whenStatusAndEnabledByFiltersProvided() {
            // Given
            StaffAccountId enablerAccountId = StaffAccountId.generate();
            StaffAccount enablerAccount = new StaffAccountFixture()
                    .withId(enablerAccountId.getValue().toString())
                    .withUsername("enabler")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount enabledActiveStaffAccount = new StaffAccountFixture()
                    .withUsername("enabled_active_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .withEnabledBy(enablerAccountId.getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(enablerAccount);
            testJdbcHelper.insertStaffAccount(enabledActiveStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.ACTIVE, Username.of("enabler"), null, null, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(enabledActiveStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnDisabledStaffAccountDisabledBySpecificUser_whenStatusAndDisabledByFiltersProvided() {
            // Given
            StaffAccountId disablerAccountId = StaffAccountId.generate();
            StaffAccount disablerAccount = new StaffAccountFixture()
                    .withId(disablerAccountId.getValue().toString())
                    .withUsername("disabler")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount disabledStaffAccount = new StaffAccountFixture()
                    .withUsername("disabled_user")
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withCreatedBy(adminId)
                    .withDisabledBy(disablerAccountId.getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(disablerAccount);
            testJdbcHelper.insertStaffAccount(disabledStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.DISABLED, null, Username.of("disabler"), null, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(disabledStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnActiveStaffAccountCreatedBeforeSpecificTime_whenStatusAndCreatedBeforeFiltersProvided() {
            // Given
            Instant beforeTime = Instant.parse("2040-06-15T12:00:00Z");
            DateTimeUtc beforeDateTime = DateTimeUtc.of(beforeTime);
            StaffAccount activeStaffAccount = new StaffAccountFixture()
                    .withUsername("active_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(activeStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.ACTIVE, null, null, beforeDateTime, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(activeStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnActiveStaffAccountCreatedAfterSpecificTime_whenStatusAndCreatedAfterFiltersProvided() {
            // Given
            Instant afterTime = Instant.parse("2024-06-10T12:00:00Z");
            DateTimeUtc afterDateTime = DateTimeUtc.of(afterTime);
            StaffAccount activeStaffAccount = new StaffAccountFixture()
                    .withUsername("active_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(activeStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.ACTIVE, null, null, null, afterDateTime);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(activeStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnStaffAccountEnabledBySpecificUserAndCreatedBeforeSpecificTime_whenEnabledByAndCreatedBeforeFiltersProvided() {
            // Given
            Instant beforeTime = Instant.parse("2040-06-15T12:00:00Z");
            DateTimeUtc beforeDateTime = DateTimeUtc.of(beforeTime);
            StaffAccountId enablerAccountId = StaffAccountId.generate();
            StaffAccount enablerAccount = new StaffAccountFixture()
                    .withId(enablerAccountId.getValue().toString())
                    .withUsername("enabler")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount enabledStaffAccount = new StaffAccountFixture()
                    .withUsername("enabled_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .withEnabledBy(enablerAccountId.getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(enablerAccount);
            testJdbcHelper.insertStaffAccount(enabledStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(null, Username.of("enabler"), null, beforeDateTime, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(enabledStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnStaffAccountDisabledBySpecificUserAndCreatedAfterSpecificTime_whenDisabledByAndCreatedAfterFiltersProvided() {
            // Given
            Instant afterTime = Instant.parse("2024-06-10T12:00:00Z");
            DateTimeUtc afterDateTime = DateTimeUtc.of(afterTime);
            StaffAccountId disablerAccountId = StaffAccountId.generate();
            StaffAccount disablerAccount = new StaffAccountFixture()
                    .withId(disablerAccountId.getValue().toString())
                    .withUsername("disabler")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount disabledStaffAccount = new StaffAccountFixture()
                    .withUsername("disabled_user")
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withCreatedBy(adminId)
                    .withDisabledBy(disablerAccountId.getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(disablerAccount);
            testJdbcHelper.insertStaffAccount(disabledStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(null, null, Username.of("disabler"), null, afterDateTime);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(disabledStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnStaffAccountInDateRange_whenCreatedBeforeAndCreatedAfterFiltersProvided() {
            // Given
            Instant afterTime = Instant.parse("2024-06-01T12:00:00Z");
            Instant beforeTime = Instant.parse("2040-06-30T12:00:00Z");
            DateTimeUtc afterDateTime = DateTimeUtc.of(afterTime);
            DateTimeUtc beforeDateTime = DateTimeUtc.of(beforeTime);
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withUsername("test_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(null, null, null, beforeDateTime, afterDateTime);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(staffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnActiveStaffAccountEnabledBySpecificUserAndCreatedBeforeSpecificTime_whenStatusEnabledByAndCreatedBeforeFiltersProvided() {
            // Given
            Instant beforeTime = Instant.parse("2040-06-15T12:00:00Z");
            DateTimeUtc beforeDateTime = DateTimeUtc.of(beforeTime);
            StaffAccountId enablerAccountId = StaffAccountId.generate();
            StaffAccount enablerAccount = new StaffAccountFixture()
                    .withId(enablerAccountId.getValue().toString())
                    .withUsername("enabler")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount enabledActiveStaffAccount = new StaffAccountFixture()
                    .withUsername("enabled_active_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .withEnabledBy(enablerAccountId.getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(enablerAccount);
            testJdbcHelper.insertStaffAccount(enabledActiveStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.ACTIVE, Username.of("enabler"), null, beforeDateTime, null);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(enabledActiveStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnActiveStaffAccountInDateRange_whenStatusAndDateRangeFiltersProvided() {
            // Given
            Instant afterTime = Instant.parse("2024-06-01T12:00:00Z");
            Instant beforeTime = Instant.parse("2040-06-30T12:00:00Z");
            DateTimeUtc afterDateTime = DateTimeUtc.of(afterTime);
            DateTimeUtc beforeDateTime = DateTimeUtc.of(beforeTime);
            StaffAccount activeStaffAccount = new StaffAccountFixture()
                    .withUsername("active_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            testJdbcHelper.insertStaffAccount(activeStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.ACTIVE, null, null, beforeDateTime, afterDateTime);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(activeStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnDisabledStaffAccountDisabledBySpecificUserInDateRange_whenStatusDisabledByAndDateRangeFiltersProvided() {
            // Given
            Instant afterTime = Instant.parse("2024-06-01T12:00:00Z");
            Instant beforeTime = Instant.parse("2040-06-30T12:00:00Z");
            DateTimeUtc afterDateTime = DateTimeUtc.of(afterTime);
            DateTimeUtc beforeDateTime = DateTimeUtc.of(beforeTime);
            StaffAccountId disablerAccountId = StaffAccountId.generate();
            StaffAccount disablerAccount = new StaffAccountFixture()
                    .withId(disablerAccountId.getValue().toString())
                    .withUsername("disabler")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount disabledStaffAccount = new StaffAccountFixture()
                    .withUsername("disabled_user")
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withCreatedBy(adminId)
                    .withDisabledBy(disablerAccountId.getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(disablerAccount);
            testJdbcHelper.insertStaffAccount(disabledStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.DISABLED, null, Username.of("disabler"), beforeDateTime, afterDateTime);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(disabledStaffAccount.getId().getValue()));
        }

        @Test
        void shouldReturnActiveStaffAccountEnabledBySpecificUserInDateRange_whenAllFiltersProvided() {
            // Given
            Instant afterTime = Instant.parse("2024-06-01T12:00:00Z");
            Instant beforeTime = Instant.parse("2040-06-30T12:00:00Z");
            DateTimeUtc afterDateTime = DateTimeUtc.of(afterTime);
            DateTimeUtc beforeDateTime = DateTimeUtc.of(beforeTime);
            StaffAccountId enablerAccountId = StaffAccountId.generate();
            StaffAccount enablerAccount = new StaffAccountFixture()
                    .withId(enablerAccountId.getValue().toString())
                    .withUsername("enabler")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .build();
            StaffAccount enabledActiveStaffAccount = new StaffAccountFixture()
                    .withUsername("enabled_active_user")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withCreatedBy(adminId)
                    .withEnabledBy(enablerAccountId.getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(enablerAccount);
            testJdbcHelper.insertStaffAccount(enabledActiveStaffAccount);

            // When
            List<StaffAccountSummaryReadModel> results = sut.findAllSummaries(StaffAccountStatus.ACTIVE, Username.of("enabler"), null, beforeDateTime, afterDateTime);

            // Then
            assertThat(results).anyMatch(s -> s.id().equals(enabledActiveStaffAccount.getId().getValue()));
        }
    }

    @Nested
    class FindDetailedById extends IntegrationTestBase {
        private final StaffAccountReadRepo sut;
        private final TestJdbcHelper testJdbcHelper;

        @Autowired
        public FindDetailedById(WriteJdbcHelper writeJdbcHelper, StaffAccountReadRepo staffAccountReadRepo) {
            sut = staffAccountReadRepo;
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnStaffAccountDetailedReadModel() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withUsername("john_doe")
                    .withEmail("john_doe@example.com")
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of("MANAGE_ACCOUNTS", "VIEW_ACCOUNTS_LIST"))
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount);

            // When
            Optional<StaffAccountDetailedReadModel> optionalStaffAccountDetailedReadModel = sut.findDetailedById(staffAccount.getId().getValue());

            // Then
            assertThat(optionalStaffAccountDetailedReadModel).isPresent();
            StaffAccountDetailedReadModel retrievedDetailedModel = optionalStaffAccountDetailedReadModel.get();
            assertThat(retrievedDetailedModel.id()).isEqualTo(staffAccount.getId().getValue());
            assertThat(retrievedDetailedModel.username()).isEqualTo(staffAccount.getUsername().getValue());
            assertThat(retrievedDetailedModel.orderAccessDurationInDays()).isEqualTo(staffAccount.getOrderAccessDuration().getValueInDays());
            assertThat(retrievedDetailedModel.modmailTranscriptAccessDurationInDays()).isEqualTo(staffAccount.getModmailTranscriptAccessDuration().getValueInDays());
            assertThat(retrievedDetailedModel.status()).isEqualTo(staffAccount.getStatus().toString());
            assertThat(retrievedDetailedModel.createdBy()).isEqualTo(staffAccount.getCreatedBy().getValue());
            assertThat(retrievedDetailedModel.permissionCodes()).isEqualTo(List.of("MANAGE_ACCOUNTS", "VIEW_ACCOUNTS_LIST"));
        }
    }
}
