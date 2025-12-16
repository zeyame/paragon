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
    void refreshTokenNotFound_shouldHaveExpectedCodeAndMessage() {
        // When
        AppExceptionInfo info = AppExceptionInfo.invalidRefreshToken();

        // Then
        assertThat(info.getMessage()).isEqualTo("The provided refresh token is invalid.");
        assertThat(info.getAppErrorCode()).isEqualTo(102);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.AUTHENTICATION_FAILED);
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

    @Test
    void invalidStaffAccountCreatedDateRange_shouldHaveExpectedCodeAndMessage() {
        // When
        AppExceptionInfo info = AppExceptionInfo.invalidStaffAccountCreatedDateRange("2024-01-01T00:00:00Z", "2024-02-01T00:00:00Z");

        // Then
        assertThat(info.getMessage()).isEqualTo("createdBefore ('2024-01-01T00:00:00Z') must be greater than or equal to createdAfter ('2024-02-01T00:00:00Z').");
        assertThat(info.getAppErrorCode()).isEqualTo(105);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.CLIENT_ERROR);
    }

    @Test
    void mutuallyExclusiveStaffAccountFilters_shouldHaveExpectedCodeAndMessage() {
        // When
        AppExceptionInfo info = AppExceptionInfo.mutuallyExclusiveStaffAccountFilters();

        // Then
        assertThat(info.getMessage()).isEqualTo("enabledBy and disabledBy filters cannot be used together.");
        assertThat(info.getAppErrorCode()).isEqualTo(106);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.CLIENT_ERROR);
    }

    @Test
    void staffAccountNotActive_shouldHaveExpectedCodeAndMessage() {
        // When
        AppExceptionInfo info = AppExceptionInfo.staffAccountNotActive("staff-1");

        // Then
        assertThat(info.getMessage()).isEqualTo("Staff account 'staff-1' must be active to perform this action.");
        assertThat(info.getAppErrorCode()).isEqualTo(107);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.PERMISSION_DENIED);
    }

    @Test
    void missingRequiredPermission_shouldHaveExpectedCodeAndMessage() {
        // When
        AppExceptionInfo info = AppExceptionInfo.missingRequiredPermission("staff-1", "MANAGE_ACCOUNTS");

        // Then
        assertThat(info.getMessage()).isEqualTo("Staff account 'staff-1' lacks permission 'MANAGE_ACCOUNTS'.");
        assertThat(info.getAppErrorCode()).isEqualTo(108);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.PERMISSION_DENIED);
    }

    @Test
    void newPasswordMatchesCurrentPassword_shouldHaveExpectedCodeAndMessage() {
        // When
        AppExceptionInfo info = AppExceptionInfo.newPasswordMatchesCurrentPassword();

        // Then
        assertThat(info.getMessage()).isEqualTo("The new password must be different from the current password.");
        assertThat(info.getAppErrorCode()).isEqualTo(109);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.INVALID_RESOURCE_STATE);
    }

    @Test
    void pendingStaffAccountRequestAlreadyExists_shouldHaveExpectedCodeAndMessage() {
        // Given
        String staffAccountUsername = "john_doe";
        String requestType = "PASSWORD_CHANGE";

        // When
        AppExceptionInfo info = AppExceptionInfo.pendingStaffAccountRequestAlreadyExists(staffAccountUsername, requestType);

        // Then
        assertThat(info.getMessage()).isEqualTo("Staff account 'john_doe' already has a pending request of type 'PASSWORD_CHANGE'.");
        assertThat(info.getAppErrorCode()).isEqualTo(110);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.RESOURCE_UNIQUENESS_VIOLATION);
    }
}
