package com.paragon.domain.exceptions.aggregate;

import com.paragon.domain.exceptions.DomainException;

public class StaffAccountRequestException extends DomainException {
    public StaffAccountRequestException(StaffAccountRequestExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}