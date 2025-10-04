package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class AuditEntryTargetIdExceptionInfo extends DomainExceptionInfo {
    private AuditEntryTargetIdExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static AuditEntryTargetIdExceptionInfo missingValue() {
        return new AuditEntryTargetIdExceptionInfo(
                "Audit entry target ID cannot be null or empty.",
                112001
        );
    }
}
