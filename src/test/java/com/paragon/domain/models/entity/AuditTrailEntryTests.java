package com.paragon.domain.models.entity;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.enums.Outcome;
import com.paragon.domain.exceptions.entity.AuditTrailEntryException;
import com.paragon.domain.exceptions.entity.AuditTrailEntryExceptionInfo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryTargetId;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.*;

public class AuditTrailEntryTests {
    @Nested
    class Create {
        private final StaffAccountId actorId;
        private final AuditEntryActionType actionType;
        private final AuditEntryTargetId targetId;
        private final AuditEntryTargetType targetType;
        private final Outcome outcome;
        private final String ipAddress;
        private final String correlationId;

        Create() {
            actorId = StaffAccountId.generate();
            actionType = AuditEntryActionType.REGISTER_ACCOUNT;
            targetId = AuditEntryTargetId.of("target-id");
            targetType = AuditEntryTargetType.ACCOUNT;
            outcome = Outcome.SUCCESS;
            ipAddress = "ip-address";
            correlationId = "correlation-id";
        }

        @Test
        void givenValidInputWithOptionalParameters_shouldCreateAuditTrailEntry() {
            // When
            AuditTrailEntry auditTrailEntry = AuditTrailEntry.create(
                    actorId, actionType, targetId, targetType, outcome, ipAddress, correlationId
            );

            // Then
            assertThat(auditTrailEntry).isNotNull();
            assertThat(auditTrailEntry.getActorId()).isEqualTo(actorId);
            assertThat(auditTrailEntry.getActionType()).isEqualTo(actionType);
            assertThat(auditTrailEntry.getTargetId()).isEqualTo(targetId);
            assertThat(auditTrailEntry.getTargetType()).isEqualTo(targetType);
            assertThat(auditTrailEntry.getOutcome()).isEqualTo(outcome);
            assertThat(auditTrailEntry.getIpAddress()).isEqualTo(ipAddress);
            assertThat(auditTrailEntry.getCorrelationId()).isEqualTo(correlationId);
        }

        @Test
        void givenValidInputWithoutOptionalParameters_shouldCreateAuditTrailEntry() {
            // When
            AuditTrailEntry auditTrailEntry = AuditTrailEntry.create(
                    actorId, actionType, null, null, outcome, "", ""
            );

            // Then
            assertThat(auditTrailEntry).isNotNull();
            assertThat(auditTrailEntry.getActorId()).isEqualTo(actorId);
            assertThat(auditTrailEntry.getActionType()).isEqualTo(actionType);
            assertThat(auditTrailEntry.getTargetId()).isNull();
            assertThat(auditTrailEntry.getTargetType()).isNull();
            assertThat(auditTrailEntry.getOutcome()).isEqualTo(outcome);
            assertThat(auditTrailEntry.getIpAddress()).isEmpty();
            assertThat(auditTrailEntry.getCorrelationId()).isEmpty();
        }

        @Test
        void shouldGenerateUniqueAuditEntryId() {
            AuditTrailEntry entry1 = AuditTrailEntry.create(actorId, actionType, targetId, targetType, outcome, ipAddress, correlationId);
            AuditTrailEntry entry2 = AuditTrailEntry.create(actorId, actionType, targetId, targetType, outcome, ipAddress, correlationId);

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
                    .isThrownBy(() -> AuditTrailEntry.create(null, actionType, targetId, targetType, outcome, ipAddress, correlationId))
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
                    .isThrownBy(() -> AuditTrailEntry.create(actorId, null, targetId, targetType, outcome, ipAddress, correlationId))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenMissingOutcome_creationShouldFail() {
            // Given
            String expectedErrorMessage = AuditTrailEntryExceptionInfo.outcomeRequired().getMessage();
            int expectedErrorCode = AuditTrailEntryExceptionInfo.outcomeRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(AuditTrailEntryException.class)
                    .isThrownBy(() -> AuditTrailEntry.create(actorId, actionType, targetId, targetType, null, ipAddress, correlationId))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }
}
