package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.FailedLoginAttemptsException;
import com.paragon.domain.exceptions.valueobject.FailedLoginAttemptsExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

public class FailedLoginAttemptsTests {
    @Nested
    class of {
        @ParameterizedTest
        @ValueSource(ints = {0, 2, 3, 5})
        void shouldCreateFailedLoginAttempts_whenAttemptNumberIsValid(int validAttemptNumber) {
            // When
            FailedLoginAttempts failedLoginAttempts = FailedLoginAttempts.of(validAttemptNumber);

            // Then
            assertThat(failedLoginAttempts.getValue()).isEqualTo(validAttemptNumber);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, FailedLoginAttempts.MAX_ATTEMPTS + 1})
        void shouldRejectOutOfRangeAttemptNumbers(int invalidAttemptNumber) {
            // Given
            String expectedErrorMessage = FailedLoginAttemptsExceptionInfo.invalidAttemptNumber().getMessage();
            int expectedErrorCode = FailedLoginAttemptsExceptionInfo.invalidAttemptNumber().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(FailedLoginAttemptsException.class)
                    .isThrownBy(() -> FailedLoginAttempts.of(invalidAttemptNumber))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }

    @Nested
    class Initial {
        @Test
        void shouldInitializeFailedLoginAttemptsToZero() {
            // Given
            FailedLoginAttempts failedLoginAttempts = FailedLoginAttempts.initial();

            // When & Then
            assertThat(failedLoginAttempts.getValue()).isZero();
        }
    }

    @Nested
    class Increment {
        @Test
        void shouldIncrementAttemptNumber_whenMaxHasNotBeenReached() {
            // Given
            FailedLoginAttempts failedLoginAttempts = FailedLoginAttempts.of(1);

            // When
            failedLoginAttempts = failedLoginAttempts.increment();

            // Then
            assertThat(failedLoginAttempts.getValue()).isEqualTo(2);
        }

        @Test
        void shouldFailToIncrement_whenMaxAttemptNumberHasBeenReached() {
            // Given
            FailedLoginAttempts failedLoginAttempts = FailedLoginAttempts.of(FailedLoginAttempts.MAX_ATTEMPTS);

            String expectedErrorMessage = FailedLoginAttemptsExceptionInfo.maxAttemptsReached().getMessage();
            int expectedErrorCode = FailedLoginAttemptsExceptionInfo.maxAttemptsReached().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(FailedLoginAttemptsException.class)
                    .isThrownBy(failedLoginAttempts::increment)
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }

    @Nested
    class Reset {
        @Test
        void shouldResetAttemptNumberToZero() {
            // Given
            FailedLoginAttempts failedLoginAttempts = FailedLoginAttempts.of(3);

            // When
            failedLoginAttempts = failedLoginAttempts.reset();

            // Then
            assertThat(failedLoginAttempts.getValue()).isZero();
        }
    }

    @Nested
    class HasReachedMax {
        @Test
        void shouldReturnTrue_whenMaxAttemptNumberHasBeenReached() {
            // Given
            FailedLoginAttempts failedLoginAttempts = FailedLoginAttempts.of(FailedLoginAttempts.MAX_ATTEMPTS);

            // When & Then
            assertThat(failedLoginAttempts.hasReachedMax()).isTrue();
        }

        @Test
        void shouldReturnFalse_whenMaxAttemptNumberHasNotBeenReached() {
            // Given
            FailedLoginAttempts failedLoginAttempts = FailedLoginAttempts.of(FailedLoginAttempts.MAX_ATTEMPTS - 1);

            // When & Then
            assertThat(failedLoginAttempts.hasReachedMax()).isFalse();
        }
    }
}
