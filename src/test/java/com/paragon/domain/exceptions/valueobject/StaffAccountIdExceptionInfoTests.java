package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class StaffAccountIdExceptionInfoTests {
    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        // Given
        StaffAccountIdExceptionInfo exceptionInfo = StaffAccountIdExceptionInfo.missingValue();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Staff account ID cannot be null or empty.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(102001);
    }

    @Test
    void invalidFormat_shouldHaveExpectedCodeAndMessage() {
        // Given
        StaffAccountIdExceptionInfo exceptionInfo = StaffAccountIdExceptionInfo.invalidFormat();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Staff account ID should be of valid UUID format.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(102002);
    }

}
