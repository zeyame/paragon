package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class EmailException extends DomainException {
    public EmailException(EmailExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
