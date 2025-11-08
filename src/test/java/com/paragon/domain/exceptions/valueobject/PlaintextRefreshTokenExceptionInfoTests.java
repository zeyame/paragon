package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaintextRefreshTokenExceptionInfoTests {
    @Test
    void missingValue_shouldHaveCorrectMessageAndErrorCode() {
        // When
        PlaintextRefreshTokenExceptionInfo result = PlaintextRefreshTokenExceptionInfo.missingValue();

        // Then
        assertThat(result.getMessage()).isEqualTo("Plaintext refresh token cannot be null or empty.");
        assertThat(result.getDomainErrorCode()).isEqualTo(114001);
    }
}