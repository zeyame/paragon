package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        PasswordExceptionInfo info = PasswordExceptionInfo.missingValue();

        assertThat(info.getMessage()).isEqualTo("Password cannot be null or empty.");
        assertThat(info.getDomainErrorCode()).isEqualTo(105001);
    }
}
