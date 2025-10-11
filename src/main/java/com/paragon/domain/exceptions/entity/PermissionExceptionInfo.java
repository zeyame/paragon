package com.paragon.domain.exceptions.entity;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class PermissionExceptionInfo extends DomainExceptionInfo {
    private PermissionExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static PermissionExceptionInfo codeRequired() {
        return new PermissionExceptionInfo(
                "Permission code is required when creating a permission.",
                200001
        );
    }

    public static PermissionExceptionInfo categoryRequired() {
        return new PermissionExceptionInfo(
                "Permission category is required when creating a permission.",
                200002
        );
    }
}
