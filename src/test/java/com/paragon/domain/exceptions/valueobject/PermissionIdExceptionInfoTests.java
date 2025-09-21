package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionIdExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        // Given
        PermissionIdExceptionInfo exceptionInfo = PermissionIdExceptionInfo.missingValue();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Permission ID cannot be null or empty.");
        assertThat(exceptionInfo.getDomainErrorCode())
                .isEqualTo(109001);
    }

    @Test
    void invalidFormat_shouldHaveExpectedCodeAndMessage() {
        // Given
        PermissionIdExceptionInfo exceptionInfo = PermissionIdExceptionInfo.invalidFormat();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Permission ID must be of valid UUID format.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(109002);
    }
}
