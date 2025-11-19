package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeUtcExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        DateTimeUtcExceptionInfo info = DateTimeUtcExceptionInfo.missingValue();

        assertThat(info.getMessage()).isEqualTo("Date/time value must not be null or blank.");
        assertThat(info.getDomainErrorCode()).isEqualTo(115001);
    }

    @Test
    void invalidFormat_shouldHaveExpectedCodeAndMessage() {
        DateTimeUtcExceptionInfo info = DateTimeUtcExceptionInfo.invalidFormat();

        assertThat(info.getMessage()).isEqualTo("Date/time value must be a valid ISO-8601 UTC timestamp (e.g. 2024-01-01T00:00:00Z).");
        assertThat(info.getDomainErrorCode()).isEqualTo(115002);
    }
}
