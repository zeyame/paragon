package com.paragon.domain.exceptions.entity;

import com.paragon.domain.exceptions.DomainException;

public class AuditTrailEntryException extends DomainException {
    public AuditTrailEntryException(AuditTrailEntryExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
