package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModmailTranscriptAccessDurationExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        ModmailTranscriptAccessDurationExceptionInfo info = ModmailTranscriptAccessDurationExceptionInfo.missingValue();

        assertThat(info.getMessage()).isEqualTo("Modmail transcript access duration cannot be null or empty.");
        assertThat(info.getDomainErrorCode()).isEqualTo(107001);
    }

    @Test
    void mustBePositive_shouldHaveExpectedCodeAndMessage() {
        ModmailTranscriptAccessDurationExceptionInfo info = ModmailTranscriptAccessDurationExceptionInfo.mustBePositive();

        assertThat(info.getMessage()).isEqualTo("Modmail transcript access duration must be greater than zero");
        assertThat(info.getDomainErrorCode()).isEqualTo(107002);
    }
}
