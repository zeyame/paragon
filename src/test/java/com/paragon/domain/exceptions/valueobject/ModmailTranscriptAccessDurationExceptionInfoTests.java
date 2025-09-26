package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModmailTranscriptAccessDurationExceptionInfoTests {

    @Test
    void mustBePositive_shouldHaveExpectedCodeAndMessage() {
        ModmailTranscriptAccessDurationExceptionInfo info = ModmailTranscriptAccessDurationExceptionInfo.mustBePositive();

        assertThat(info.getMessage()).isEqualTo("Modmail transcript access duration must be greater than zero");
        assertThat(info.getDomainErrorCode()).isEqualTo(107001);
    }
}
