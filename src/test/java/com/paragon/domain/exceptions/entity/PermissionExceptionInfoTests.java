package com.paragon.domain.exceptions.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionExceptionInfoTests {

    @Test
    void codeRequired_shouldHaveExpectedCodeAndMessage() {
        // When
        PermissionExceptionInfo exceptionInfo = PermissionExceptionInfo.codeRequired();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Permission code is required when creating a permission.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(200001);
    }

    @Test
    void categoryRequired_shouldHaveExpectedCodeAndMessage() {
        // When
        PermissionExceptionInfo exceptionInfo = PermissionExceptionInfo.categoryRequired();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Permission category is required when creating a permission.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(200002);
    }
}
