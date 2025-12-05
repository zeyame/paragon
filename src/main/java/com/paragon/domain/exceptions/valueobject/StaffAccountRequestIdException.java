package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.exceptions.DomainExceptionInfo;

public class StaffAccountRequestIdException extends DomainException {
    public StaffAccountRequestIdException(StaffAccountRequestIdExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
