package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        EmailExceptionInfo info = EmailExceptionInfo.missingValue();

        assertThat(info.getMessage()).isEqualTo("Email address must not be null or empty.");
        assertThat(info.getDomainErrorCode()).isEqualTo(104001);
    }

    @Test
    void lengthOutOfRange_shouldHaveExpectedCodeAndMessage() {
        EmailExceptionInfo info = EmailExceptionInfo.lengthOutOfRange();

        assertThat(info.getMessage()).isEqualTo("Email address must not exceed 320 characters.");
        assertThat(info.getDomainErrorCode()).isEqualTo(104002);
    }

    @Test
    void invalidFormat_shouldHaveExpectedCodeAndMessage() {
        EmailExceptionInfo info = EmailExceptionInfo.invalidFormat();

        assertThat(info.getMessage()).isEqualTo("Email address has an invalid format (must be a valid address like user@example.com).");
        assertThat(info.getDomainErrorCode()).isEqualTo(104003);
    }
}
