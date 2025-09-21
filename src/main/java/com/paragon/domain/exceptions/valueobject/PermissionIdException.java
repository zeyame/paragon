package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class PermissionIdException extends DomainException {
    public PermissionIdException(PermissionIdExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
