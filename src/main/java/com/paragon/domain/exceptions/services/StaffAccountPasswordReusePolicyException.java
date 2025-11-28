package com.paragon.domain.exceptions.services;

import com.paragon.domain.exceptions.DomainException;

public class StaffAccountPasswordReusePolicyException extends DomainException {

    public StaffAccountPasswordReusePolicyException(StaffAccountPasswordReusePolicyExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
