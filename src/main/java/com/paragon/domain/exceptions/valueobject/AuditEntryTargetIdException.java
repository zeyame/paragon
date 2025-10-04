package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.exceptions.DomainExceptionInfo;

public class AuditEntryTargetIdException extends DomainException {
    public AuditEntryTargetIdException(AuditEntryTargetIdExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
