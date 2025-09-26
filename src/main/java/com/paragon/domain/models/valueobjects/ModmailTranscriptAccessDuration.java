package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.ModmailTranscriptAccessDurationException;
import com.paragon.domain.exceptions.valueobject.ModmailTranscriptAccessDurationExceptionInfo;

import java.time.Duration;
import java.util.List;

public class ModmailTranscriptAccessDuration extends ValueObject {

    private final Duration value;

    private ModmailTranscriptAccessDuration(Duration value) {
        this.value = value;
    }

    public static ModmailTranscriptAccessDuration from(int value) {
        assertValidDuration(value);
        return new ModmailTranscriptAccessDuration(Duration.ofDays(value));
    }

    public Duration getValue() {
        return value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidDuration(int value) {
        if (value <= 0) {
            throw new ModmailTranscriptAccessDurationException(ModmailTranscriptAccessDurationExceptionInfo.mustBePositive());
        }
    }
}
