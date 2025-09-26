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

    public static AppExceptionInfo permissionAccessDenied(String action) {
        return new AppExceptionInfo(
                String.format("Staff account does not have permission to perform action '%s'.", action),
                102,
                AppExceptionStatusCode.PERMISSION_DENIED
        );
    }


}
