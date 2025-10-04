package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class AuditEntryIdExceptionInfo extends DomainExceptionInfo {
    private AuditEntryIdExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static AuditEntryIdExceptionInfo missingValue() {
        return new AuditEntryIdExceptionInfo(
                "AuditTrailEntryId value is required and cannot be missing.",
                111001
        );
    }


    public static AuditEntryIdExceptionInfo invalidFormat() {
        return new AuditEntryIdExceptionInfo(
                "AuditTrailEntryId format is invalid. A valid UUID string is required.",
                111002
        );
    }
}
