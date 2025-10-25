package com.paragon.application.common.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AppExceptionInfoTests {

    @Test
    void staffAccountNotFound_shouldHaveExpectedCodeAndMessage() {
        // Given
        String id = "12345";

        // When
        AppExceptionInfo info = AppExceptionInfo.staffAccountNotFound(id);

        // Then
        assertThat(info.getMessage()).isEqualTo("Staff account with id '12345' was not found.");
        assertThat(info.getAppErrorCode()).isEqualTo(101);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.RESOURCE_NOT_FOUND);
    }

    @Test
    void permissionAccessDenied_shouldHaveExpectedCodeAndMessage() {
        // Given
        String action = "delete-staff";

        // When
        AppExceptionInfo info = AppExceptionInfo.permissionAccessDenied(action);

        // Then
        assertThat(info.getMessage()).isEqualTo("Staff account does not have permission to perform action 'delete-staff'.");
        assertThat(info.getAppErrorCode()).isEqualTo(102);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.PERMISSION_DENIED);
    }

    @Test
    void staffAccountUsernameAlreadyExists_shouldHaveExpectedCodeAndMessage() {
        // Given
        String username = "admin";

        // When
        AppExceptionInfo info = AppExceptionInfo.staffAccountUsernameAlreadyExists(username);

        // Then
        assertThat(info.getMessage()).isEqualTo("A staff account with username 'admin' already exists.");
        assertThat(info.getAppErrorCode()).isEqualTo(103);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.RESOURCE_UNIQUENESS_VIOLATION);
    }

    @Test
    void invalidLoginCredentials_shouldHaveExpectedCodeAndMessage() {
        // When
        AppExceptionInfo info = AppExceptionInfo.invalidLoginCredentials();

        // Then
        assertThat(info.getMessage()).isEqualTo("Invalid username or password");
        assertThat(info.getAppErrorCode()).isEqualTo(104);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.AUTHENTICATION_FAILED);
    }
}
