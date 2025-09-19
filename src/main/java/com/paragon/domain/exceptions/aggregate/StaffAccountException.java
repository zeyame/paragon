package com.paragon.domain.exceptions.aggregate;

import com.paragon.domain.exceptions.DomainException;

public class StaffAccountException extends DomainException {
    public StaffAccountException(StaffAccountExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
