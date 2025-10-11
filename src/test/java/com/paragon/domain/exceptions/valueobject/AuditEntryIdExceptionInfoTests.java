package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEntryIdExceptionInfoTests {
    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        // When
        AuditEntryIdExceptionInfo exceptionInfo = AuditEntryIdExceptionInfo.missingValue();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("AuditTrailEntryId value is required and cannot be missing.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(111001);
    }

    @Test
    void invalidFormat_shouldHaveExpectedCodeAndMessage() {
        // When
        AuditEntryIdExceptionInfo exceptionInfo = AuditEntryIdExceptionInfo.invalidFormat();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("AuditTrailEntryId format is invalid. A valid UUID string is required.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(111002);
    }
}
