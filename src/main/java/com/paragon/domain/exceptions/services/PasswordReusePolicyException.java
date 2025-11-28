package com.paragon.domain.exceptions.services;

import com.paragon.domain.exceptions.DomainException;

public class PasswordReusePolicyException extends DomainException {

    public PasswordReusePolicyException(PasswordReusePolicyExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
