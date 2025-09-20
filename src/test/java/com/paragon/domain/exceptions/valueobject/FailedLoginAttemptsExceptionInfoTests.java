package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FailedLoginAttemptsExceptionInfoTests {

    @Test
    void invalidAttemptNumber_shouldHaveExpectedCodeAndMessage() {
        FailedLoginAttemptsExceptionInfo info = FailedLoginAttemptsExceptionInfo.invalidAttemptNumber();

        assertThat(info.getMessage()).isEqualTo("Failed login attempt number must be a value between 0 and 5");
        assertThat(info.getDomainErrorCode()).isEqualTo(108001);
    }

    @Test
    void maxAttemptsReached_shouldHaveExpectedCodeAndMessage() {
        FailedLoginAttemptsExceptionInfo info =
                FailedLoginAttemptsExceptionInfo.maxAttemptsReached();

        assertThat(info.getMessage()).isEqualTo("Maximum number of failed login attempts has been reached.");
        assertThat(info.getDomainErrorCode()).isEqualTo(108002);
    }
}
