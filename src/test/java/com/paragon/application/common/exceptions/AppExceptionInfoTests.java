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
        AppExceptionInfo info = AppExceptionInfo.refreshTokenNotFound();

        // Then
        assertThat(info.getMessage()).isEqualTo("The provided refresh token does not exist.");
        assertThat(info.getAppErrorCode()).isEqualTo(102);
        assertThat(info.getStatusCode()).isEqualTo(AppExceptionStatusCode.RESOURCE_NOT_FOUND);
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
}
