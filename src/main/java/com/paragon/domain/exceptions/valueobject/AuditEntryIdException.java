package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class AuditEntryIdException extends DomainException {
    public AuditEntryIdException(AuditEntryIdExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
