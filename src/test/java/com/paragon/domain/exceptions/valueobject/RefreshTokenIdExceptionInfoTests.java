package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenIdExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        RefreshTokenIdExceptionInfo info = RefreshTokenIdExceptionInfo.missingValue();

        assertThat(info.getMessage()).isEqualTo("Refresh token ID cannot be null or empty.");
        assertThat(info.getDomainErrorCode()).isEqualTo(113001);
    }

    @Test
    void invalidFormat_shouldHaveExpectedCodeAndMessage() {
        RefreshTokenIdExceptionInfo info = RefreshTokenIdExceptionInfo.invalidFormat();

        assertThat(info.getMessage()).isEqualTo("Refresh token ID should be of valid UUID format.");
        assertThat(info.getDomainErrorCode()).isEqualTo(113002);
    }
}