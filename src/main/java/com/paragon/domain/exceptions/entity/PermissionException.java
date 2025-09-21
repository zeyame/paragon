package com.paragon.domain.exceptions.entity;

import com.paragon.domain.exceptions.DomainException;

public class PermissionException extends DomainException {
    public PermissionException(PermissionExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
