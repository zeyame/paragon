package com.paragon.integration.persistence;

import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.AuditTrailEntryFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.AuditTrailWriteRepoImpl;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class AuditTrailWriteRepoTests {
    @Nested
    class Create extends IntegrationTestBase {
        private final AuditTrailWriteRepoImpl sut;
        private final TestJdbcHelper testJdbcHelper;
        private final StaffAccount adminStaffAccount;

        @Autowired
        public Create(WriteJdbcHelper writeJdbcHelper) {
            sut = new AuditTrailWriteRepoImpl(writeJdbcHelper);
            testJdbcHelper = new TestJdbcHelper(writeJdbcHelper);

            adminStaffAccount = testJdbcHelper.getStaffAccountByUsername(Username.of("admin")).get();
        }

        @Test
        void shouldInsertAuditTrailEntry() {
            // Given
            AuditTrailEntry insertedAuditTrailEntry = new AuditTrailEntryFixture()
                    .withActorId(adminStaffAccount.getId().getValue().toString())
                    .build();

            // When
            sut.create(insertedAuditTrailEntry);

            // Then
            Optional<AuditTrailEntry> optionalAuditTrailEntry = testJdbcHelper.getAuditTrailEntryById(insertedAuditTrailEntry.getId());
            assertThat(optionalAuditTrailEntry).isPresent();

            AuditTrailEntry loadedAuditTrailEntry = optionalAuditTrailEntry.get();
            assertThat(loadedAuditTrailEntry)
                    .usingRecursiveComparison()
                    .isEqualTo(insertedAuditTrailEntry);
        }

        @Test
        void shouldInsertAuditTrailEntry_withNullTargetId() {
            // Given
            AuditTrailEntry insertedAuditTrailEntry = new AuditTrailEntryFixture()
                    .withActorId(adminStaffAccount.getId().getValue().toString())
                    .withTargetId(null)
                    .build();

            // When
            sut.create(insertedAuditTrailEntry);

            // Then
            Optional<AuditTrailEntry> optionalAuditTrailEntry = testJdbcHelper.getAuditTrailEntryById(insertedAuditTrailEntry.getId());
            assertThat(optionalAuditTrailEntry).isPresent();

            AuditTrailEntry loadedAuditTrailEntry = optionalAuditTrailEntry.get();
            assertThat(loadedAuditTrailEntry.getTargetId()).isNull();
            assertThat(loadedAuditTrailEntry)
                    .usingRecursiveComparison()
                    .isEqualTo(insertedAuditTrailEntry);
        }

        @Test
        void shouldInsertAuditTrailEntry_withNullTargetType() {
            // Given
            AuditTrailEntry insertedAuditTrailEntry = new AuditTrailEntryFixture()
                    .withActorId(adminStaffAccount.getId().getValue().toString())
                    .withTargetType(null)
                    .build();

            // When
            sut.create(insertedAuditTrailEntry);

            // Then
            Optional<AuditTrailEntry> optionalAuditTrailEntry = testJdbcHelper.getAuditTrailEntryById(insertedAuditTrailEntry.getId());
            assertThat(optionalAuditTrailEntry).isPresent();

            AuditTrailEntry loadedAuditTrailEntry = optionalAuditTrailEntry.get();
            assertThat(loadedAuditTrailEntry.getTargetType()).isNull();
            assertThat(loadedAuditTrailEntry)
                    .usingRecursiveComparison()
                    .isEqualTo(insertedAuditTrailEntry);
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            AuditTrailEntry auditTrailEntry = new AuditTrailEntryFixture()
                    .withActorId(adminStaffAccount.getId().getValue().toString())
                    .build();
            testJdbcHelper.insertAuditTrailEntry(auditTrailEntry);

            // When & Then
            assertThatThrownBy(() -> sut.create(auditTrailEntry)) // inserting the entry for a second time
                    .isInstanceOf(InfraException.class);
        }
    }
}