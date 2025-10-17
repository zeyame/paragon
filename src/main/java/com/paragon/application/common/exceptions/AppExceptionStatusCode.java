package com.paragon.application.common.exceptions;

public enum AppExceptionStatusCode {
    CLIENT_ERROR(1001),
    INVALID_RESOURCE_STATE(1002),
    SERVER_ERROR(1003),
    RESOURCE_NOT_FOUND(1004),
    RESOURCE_OWNERSHIP_VIOLATION(1005),
    RESOURCE_UNIQUENESS_VIOLATION(1006),
    PERMISSION_DENIED(1007),
    AUTHENTICATION_FAILED(1008),
    UNHANDLED_ERROR(1999);

    AppExceptionStatusCode(int code) {
    }
}
