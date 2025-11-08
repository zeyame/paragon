package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenHashExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        RefreshTokenHashExceptionInfo info = RefreshTokenHashExceptionInfo.missingValue();

        assertThat(info.getMessage()).isEqualTo("Refresh token hash cannot be null or empty.");
        assertThat(info.getDomainErrorCode()).isEqualTo(114001);
    }
}