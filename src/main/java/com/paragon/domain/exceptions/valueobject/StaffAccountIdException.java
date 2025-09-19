package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class StaffAccountIdException extends DomainException {
    public StaffAccountIdException(StaffAccountIdExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
