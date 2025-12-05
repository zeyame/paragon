package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountRequestIdExceptionInfoTests {
    @Test
    void missingValue_shouldHaveExpectedMessageAndCode() {
        // When
        StaffAccountRequestIdExceptionInfo exceptionInfo = StaffAccountRequestIdExceptionInfo.missingValue();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Staff account request ID cannot be null or empty.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(102001);
    }
}
