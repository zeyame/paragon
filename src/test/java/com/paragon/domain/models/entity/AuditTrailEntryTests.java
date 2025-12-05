package com.paragon.domain.models.entity;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.TargetType;
import com.paragon.domain.exceptions.entity.AuditTrailEntryException;
import com.paragon.domain.exceptions.entity.AuditTrailEntryExceptionInfo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.TargetId;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.*;

public class AuditTrailEntryTests {
    @Nested
    class Create {
        private final StaffAccountId actorId;
        private final AuditEntryActionType actionType;
        private final TargetId targetId;
        private final TargetType targetType;

        Create() {
            actorId = StaffAccountId.generate();
            actionType = AuditEntryActionType.REGISTER_ACCOUNT;
            targetId = TargetId.of("target-id");
            targetType = TargetType.ACCOUNT;
        }

        @Test
        void givenValidInputWithOptionalParameters_shouldCreateAuditTrailEntry() {
            // When
            AuditTrailEntry auditTrailEntry = AuditTrailEntry.create(
                    actorId, actionType, targetId, targetType
            );

            // Then
            assertThat(auditTrailEntry).isNotNull();
            assertThat(auditTrailEntry.getActorId()).isEqualTo(actorId);
            assertThat(auditTrailEntry.getActionType()).isEqualTo(actionType);
            assertThat(auditTrailEntry.getTargetId()).isEqualTo(targetId);
            assertThat(auditTrailEntry.getTargetType()).isEqualTo(targetType);
        }

        @Test
        void givenValidInputWithoutOptionalParameters_shouldCreateAuditTrailEntry() {
            // When
            AuditTrailEntry auditTrailEntry = AuditTrailEntry.create(
                    actorId, actionType, null, null
            );

            // Then
            assertThat(auditTrailEntry).isNotNull();
            assertThat(auditTrailEntry.getActorId()).isEqualTo(actorId);
            assertThat(auditTrailEntry.getActionType()).isEqualTo(actionType);
            assertThat(auditTrailEntry.getTargetId()).isNull();
            assertThat(auditTrailEntry.getTargetType()).isNull();
        }

        @Test
        void shouldGenerateUniqueAuditEntryId() {
            AuditTrailEntry entry1 = AuditTrailEntry.create(actorId, actionType, targetId, targetType);
            AuditTrailEntry entry2 = AuditTrailEntry.create(actorId, actionType, targetId, targetType);

            assertThat(entry1.getId()).isNotNull();
            assertThat(entry2.getId()).isNotNull();
            assertThat(entry1.getId()).isNotEqualTo(entry2.getId());
        }

        @Test
        void givenMissingActorId_creationShouldFail() {
            // Given
            String expectedErrorMessage = AuditTrailEntryExceptionInfo.actorIdRequired().getMessage();
            int expectedErrorCode = AuditTrailEntryExceptionInfo.actorIdRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(AuditTrailEntryException.class)
                    .isThrownBy(() -> AuditTrailEntry.create(null, actionType, targetId, targetType))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenMissingActionType_creationShouldFail() {
            // Given
            String expectedErrorMessage = AuditTrailEntryExceptionInfo.actionTypeRequired().getMessage();
            int expectedErrorCode = AuditTrailEntryExceptionInfo.actionTypeRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(AuditTrailEntryException.class)
                    .isThrownBy(() -> AuditTrailEntry.create(actorId, null, targetId, targetType))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }
}
