package com.paragon.domain.exceptions.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AuditTrailEntryExceptionInfoTests {
    @Test
    void actorIdRequired_shouldHaveExpectedCodeAndMessage() {
        // When
        AuditTrailEntryExceptionInfo exceptionInfo = AuditTrailEntryExceptionInfo.actorIdRequired();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("ActorId is required for creating an AuditTrailEntry. The user performing the action must be identified.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(201001);
    }

    @Test
    void actionTypeRequired_shouldHaveExpectedCodeAndMessage() {
        // When
        AuditTrailEntryExceptionInfo exceptionInfo = AuditTrailEntryExceptionInfo.actionTypeRequired();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("ActionType is required for creating an AuditTrailEntry. The performed action must be explicitly identified.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(201002);
    }
}
