package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class StaffAccountPasswordHistoryException extends DomainException {
    public StaffAccountPasswordHistoryException(StaffAccountPasswordHistoryExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
