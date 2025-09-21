package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class PermissionCodeException extends DomainException {
    public PermissionCodeException(PermissionCodeExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
