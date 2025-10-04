package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.ModmailTranscriptAccessDurationException;
import com.paragon.domain.exceptions.valueobject.ModmailTranscriptAccessDurationExceptionInfo;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

@Getter
public class ModmailTranscriptAccessDuration extends ValueObject {
    private final Duration value;

    private ModmailTranscriptAccessDuration(Duration value) {
        this.value = value;
    }

    public static ModmailTranscriptAccessDuration from(int value) {
        assertValidDuration(value);
        return new ModmailTranscriptAccessDuration(Duration.ofDays(value));
    }

    public long getValueInDays() {
        return value.toDays();
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
