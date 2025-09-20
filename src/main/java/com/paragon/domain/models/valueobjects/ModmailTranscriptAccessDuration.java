package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.ModmailTranscriptAccessDurationException;
import com.paragon.domain.exceptions.valueobject.ModmailTranscriptAccessDurationExceptionInfo;
import com.paragon.domain.exceptions.valueobject.OrderAccessDurationException;
import com.paragon.domain.exceptions.valueobject.OrderAccessDurationExceptionInfo;

import java.time.Duration;
import java.util.List;

public class ModmailTranscriptAccessDuration extends ValueObject {

    private final Duration value;

    private ModmailTranscriptAccessDuration(Duration value) {
        this.value = value;
    }

    public static ModmailTranscriptAccessDuration of(Duration value) {
        assertValidDuration(value);
        return new ModmailTranscriptAccessDuration(value);
    }

    public Duration getValue() {
        return value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of();
    }

    private static void assertValidDuration(Duration value) {
        if (value == null) {
            throw new ModmailTranscriptAccessDurationException(ModmailTranscriptAccessDurationExceptionInfo.missingValue());
        }
        if (value.isNegative() || value.isZero()) {
            throw new ModmailTranscriptAccessDurationException(ModmailTranscriptAccessDurationExceptionInfo.mustBePositive());
        }
    }
}
