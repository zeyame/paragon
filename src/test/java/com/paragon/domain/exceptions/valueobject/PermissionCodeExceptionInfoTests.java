package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionCodeExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        // Given
        PermissionCodeExceptionInfo info = PermissionCodeExceptionInfo.missingValue();

        // Then
        assertThat(info.getMessage()).isEqualTo("Permission code cannot be null or empty.");
        assertThat(info.getDomainErrorCode())
                .isEqualTo(110001);
    }

    @Test
    void lengthOutOfRange_shouldHaveExpectedCodeAndMessage() {
        // Given
        PermissionCodeExceptionInfo info = PermissionCodeExceptionInfo.lengthOutOfRange();

        // Then
        assertThat(info.getMessage()).isEqualTo("Permission code must be between 3 and 50 characters.");
        assertThat(info.getDomainErrorCode()).isEqualTo(110002);
    }

    @Test
    void invalidCharacters_shouldHaveExpectedCodeAndMessage() {
        // Given
        PermissionCodeExceptionInfo info = PermissionCodeExceptionInfo.invalidCharacters();

        // Then
        assertThat(info.getMessage()).isEqualTo("Permission code may only contain uppercase letters, numbers, and underscores.");
        assertThat(info.getDomainErrorCode()).isEqualTo(110003);
    }

    @Test
    void mustNotStartOrEndWithUnderscore_shouldHaveExpectedCodeAndMessage() {
        // Given
        PermissionCodeExceptionInfo info = PermissionCodeExceptionInfo.mustNotStartOrEndWithUnderscore();

        // Then
        assertThat(info.getMessage()).isEqualTo("Permission code must not start or end with an underscore.");
        assertThat(info.getDomainErrorCode()).isEqualTo(110004);
    }

    @Test
    void consecutiveUnderscores_shouldHaveExpectedCodeAndMessage() {
        // Given
        PermissionCodeExceptionInfo info = PermissionCodeExceptionInfo.consecutiveUnderscores();

        // Then
        assertThat(info.getMessage()).isEqualTo("Permission code must not contain consecutive underscores.");
        assertThat(info.getDomainErrorCode()).isEqualTo(110005);
    }
}
