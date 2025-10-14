package com.paragon.integration.persistence;

import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.StaffAccountWriteRepoImpl;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class StaffAccountWriteRepoTests {
    @Nested
    class Create extends IntegrationTestBase {
        private final StaffAccountWriteRepoImpl sut;
        private final TestJdbcHelper testJdbcHelper;
        private final StaffAccount adminStaffAccount;

        @Autowired
        public Create(WriteJdbcHelper writeJdbcHelper) {
            sut = new StaffAccountWriteRepoImpl(writeJdbcHelper);
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);

            adminStaffAccount = testJdbcHelper.getStaffAccountByUsername(Username.of("admin")).get();
        }

        @Test
        void shouldInsertStaffAccount() {
            // Given
            StaffAccount insertedStaffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminStaffAccount.getId().getValue().toString())
                    .build();

            // When
            sut.create(insertedStaffAccount);

            // Then
            Optional<StaffAccount> optionalStaffAccount = testJdbcHelper.getStaffAccountById(insertedStaffAccount.getId());
            assertThat(optionalStaffAccount).isPresent();

            StaffAccount loadedStaffAccount = optionalStaffAccount.get();
            assertThat(loadedStaffAccount)
                    .usingRecursiveComparison()
                    .isEqualTo(insertedStaffAccount);

            List<PermissionCode> insertedPermissions = testJdbcHelper.getPermissionsForStaff(insertedStaffAccount.getId());
            assertThat(insertedPermissions)
                    .containsExactlyInAnyOrderElementsOf(insertedStaffAccount.getPermissionCodes());
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminStaffAccount.getId().getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(staffAccount);

            // When & Then
            assertThatThrownBy(() -> sut.create(staffAccount)) // inserting the account for a second time
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class GetById extends IntegrationTestBase {
        private final StaffAccountWriteRepoImpl sut;
        private final TestJdbcHelper testJdbcHelper;
        private final StaffAccount adminStaffAccount;

        @Autowired
        public GetById(WriteJdbcHelper writeJdbcHelper) {
            sut = new StaffAccountWriteRepoImpl(writeJdbcHelper);
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);
            adminStaffAccount = testJdbcHelper.getStaffAccountByUsername(Username.of("admin")).get();
        }

        @Test
        void shouldGetStaffAccountById() {
            // Given
            StaffAccount insertedStaffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminStaffAccount.getId().getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(insertedStaffAccount);

            // When
            Optional<StaffAccount> optionalStaffAccount = sut.getById(insertedStaffAccount.getId());

            // Then
            assertThat(optionalStaffAccount).isPresent();

            StaffAccount loadedStaffAccount = optionalStaffAccount.get();
            assertThat(loadedStaffAccount)
                    .usingRecursiveComparison()
                    .isEqualTo(insertedStaffAccount);
        }

        @Test
        void shouldReturnEmptyOptional_whenStaffAccountDoesNotExist() {
            // Given
            StaffAccountId nonExistentId = StaffAccountId.generate();

            // When
            Optional<StaffAccount> optionalStaffAccount = sut.getById(nonExistentId);

            // Then
            assertThat(optionalStaffAccount).isEmpty();
        }
    }

    @Nested
    class GetByUsername extends IntegrationTestBase {
        private final StaffAccountWriteRepoImpl sut;
        private final TestJdbcHelper testJdbcHelper;
        private final StaffAccount adminStaffAccount;

        @Autowired
        public GetByUsername(WriteJdbcHelper writeJdbcHelper) {
            sut = new StaffAccountWriteRepoImpl(writeJdbcHelper);
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);
            adminStaffAccount = testJdbcHelper.getStaffAccountByUsername(Username.of("admin")).get();
        }

        @Test
        void shouldGetStaffAccountByUsername() {
            // Given
            StaffAccount insertedStaffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminStaffAccount.getId().getValue().toString())
                    .build();
            testJdbcHelper.insertStaffAccount(insertedStaffAccount);

            // When
            Optional<StaffAccount> optionalStaffAccount = sut.getByUsername(insertedStaffAccount.getUsername());

            // Then
            assertThat(optionalStaffAccount).isPresent();

            StaffAccount loadedStaffAccount = optionalStaffAccount.get();
            assertThat(loadedStaffAccount)
                    .usingRecursiveComparison()
                    .isEqualTo(insertedStaffAccount);
        }

        @Test
        void shouldReturnEmptyOptional_whenStaffAccountDoesNotExist() {
            // Given
            Username nonExistentUsername = Username.of("nonexistent_user");

            // When
            Optional<StaffAccount> optionalStaffAccount = sut.getByUsername(nonExistentUsername);

            // Then
            assertThat(optionalStaffAccount).isEmpty();
        }
    }
}
