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

    @Test
    void invalidFormat_shouldHaveExpectedCodeAndMessage() {
        RefreshTokenHashExceptionInfo info = RefreshTokenHashExceptionInfo.invalidFormat();

        assertThat(info.getMessage()).isEqualTo("Plain refresh token must be of valid UUID format.");
        assertThat(info.getDomainErrorCode()).isEqualTo(114002);
    }
}