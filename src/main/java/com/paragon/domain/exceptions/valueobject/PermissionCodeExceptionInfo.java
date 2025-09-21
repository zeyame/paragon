package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class PermissionCodeExceptionInfo extends DomainExceptionInfo {
    private PermissionCodeExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static PermissionCodeExceptionInfo missingValue() {
        return new PermissionCodeExceptionInfo(
                "Permission code cannot be null or empty.",
                110001
        );
    }

    public static PermissionCodeExceptionInfo lengthOutOfRange() {
        return new PermissionCodeExceptionInfo(
                "Permission code must be between 3 and 50 characters.",
                110002
        );
    }

    public static PermissionCodeExceptionInfo invalidCharacters() {
        return new PermissionCodeExceptionInfo(
                "Permission code may only contain uppercase letters, numbers, and underscores.",
                110003
        );
    }

    public static PermissionCodeExceptionInfo mustNotStartOrEndWithUnderscore() {
        return new PermissionCodeExceptionInfo(
                "Permission code must not start or end with an underscore.",
                110004
        );
    }

    public static PermissionCodeExceptionInfo consecutiveUnderscores() {
        return new PermissionCodeExceptionInfo(
                "Permission code must not contain consecutive underscores.",
                110005
        );
    }
}

