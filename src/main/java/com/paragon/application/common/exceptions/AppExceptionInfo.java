package com.paragon.application.common.exceptions;

import lombok.Getter;

@Getter
public class AppExceptionInfo {
    private final String message;
    private final int appErrorCode;
    private final AppExceptionStatusCode statusCode;

    private AppExceptionInfo(String message, int appErrorCode, AppExceptionStatusCode statusCode) {
        this.message = message;
        this.appErrorCode = appErrorCode;
        this.statusCode = statusCode;
    }

    public static AppExceptionInfo staffAccountNotFound(String id) {
        return new AppExceptionInfo(
                String.format("Staff account with id '%s' was not found.", id),
                101,
                AppExceptionStatusCode.RESOURCE_NOT_FOUND
        );
    }

    public static AppExceptionInfo refreshTokenNotFound() {
        return new AppExceptionInfo(
                "The provided refresh token does not exist.",
                102,
                AppExceptionStatusCode.RESOURCE_NOT_FOUND
        );
    }

    public static AppExceptionInfo staffAccountUsernameAlreadyExists(String username) {
        return new AppExceptionInfo(
                String.format("A staff account with username '%s' already exists.", username),
                103,
                AppExceptionStatusCode.RESOURCE_UNIQUENESS_VIOLATION
        );
    }

    public static AppExceptionInfo invalidLoginCredentials() {
        return new AppExceptionInfo(
                "Invalid username or password",
                104,
                AppExceptionStatusCode.AUTHENTICATION_FAILED
        );
    }

    public static AppExceptionInfo invalidStaffAccountCreatedDateRange(String createdBefore, String createdAfter) {
        return new AppExceptionInfo(
                String.format("createdBefore ('%s') must be greater than or equal to createdAfter ('%s').", createdBefore, createdAfter),
                105,
                AppExceptionStatusCode.CLIENT_ERROR
        );
    }

    public static AppExceptionInfo mutuallyExclusiveStaffAccountFilters() {
        return new AppExceptionInfo(
                "enabledBy and disabledBy filters cannot be used together.",
                106,
                AppExceptionStatusCode.CLIENT_ERROR
        );
    }
}
