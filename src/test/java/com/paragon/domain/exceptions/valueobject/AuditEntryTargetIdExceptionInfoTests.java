package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AuditEntryTargetIdExceptionInfoTests {
    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        // When
        AuditEntryTargetIdExceptionInfo exceptionInfo = AuditEntryTargetIdExceptionInfo.missingValue();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Audit entry target ID cannot be null or empty.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(112001);
    }
}
