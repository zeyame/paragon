package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventIdExceptionInfoTests {
    @Test
    void mustNotBeNull_shouldHaveExpectedCodeAndMessage() {
        // Given
        EventIdExceptionInfo exceptionInfo = EventIdExceptionInfo.mustNotBeNull();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Event ID cannot be null and must be of valid format.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(101001);
    }
}
