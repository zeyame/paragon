package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class PermissionIdExceptionInfo extends DomainExceptionInfo {
    private PermissionIdExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static PermissionIdExceptionInfo missingValue() {
        return new PermissionIdExceptionInfo(
                "Permission ID cannot be null or empty.",
                109001
        );
    }

    public static PermissionIdExceptionInfo invalidFormat() {
        return new PermissionIdExceptionInfo(
                "Permission ID must be of valid UUID format.",
                109002
        );
    }
}
