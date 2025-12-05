package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class TargetIdExceptionInfo extends DomainExceptionInfo {
    private TargetIdExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static TargetIdExceptionInfo missingValue() {
        return new TargetIdExceptionInfo(
                "Audit entry target ID cannot be null or empty.",
                112001
        );
    }
}
