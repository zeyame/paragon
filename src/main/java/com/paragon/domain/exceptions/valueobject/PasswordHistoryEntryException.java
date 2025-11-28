package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class PasswordHistoryEntryException extends DomainException {
    public PasswordHistoryEntryException(PasswordHistoryEntryExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
