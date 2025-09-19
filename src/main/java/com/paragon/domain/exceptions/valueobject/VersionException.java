package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class VersionException extends DomainException {
    public VersionException(VersionExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
