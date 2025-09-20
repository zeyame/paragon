package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class ModmailTranscriptAccessDurationExceptionInfo extends DomainExceptionInfo {
    private ModmailTranscriptAccessDurationExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static ModmailTranscriptAccessDurationExceptionInfo missingValue() {
        return new ModmailTranscriptAccessDurationExceptionInfo(
                "Modmail transcript access duration cannot be null or empty.",
                107001
        );
    }

    public static ModmailTranscriptAccessDurationExceptionInfo mustBePositive() {
        return new ModmailTranscriptAccessDurationExceptionInfo(
                "Modmail transcript access duration must be greater than zero",
                107002
        );
    }
}
