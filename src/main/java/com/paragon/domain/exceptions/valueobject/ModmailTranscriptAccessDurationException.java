package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainException;

public class ModmailTranscriptAccessDurationException extends DomainException {

    public ModmailTranscriptAccessDurationException(ModmailTranscriptAccessDurationExceptionInfo exceptionInfo) {
        super(exceptionInfo);
    }
}
