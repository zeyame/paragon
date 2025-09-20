package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.OrderAccessDurationException;
import com.paragon.domain.exceptions.valueobject.OrderAccessDurationExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.stream.Stream;

public class OrderAccessDurationTests {
    @Nested
    class Of {
        @ParameterizedTest
        @ValueSource(ints = {3, 5, 10, 20, 28})
        void shouldCreateValidDuration(int validDuration) {
            // Given
            Duration duration = Duration.ofDays(validDuration);

            // When
            OrderAccessDuration orderAccessDuration = OrderAccessDuration.of(duration);

            // Then
            assertThat(orderAccessDuration.getValue()).isEqualTo(duration);
        }

        @ParameterizedTest
        @MethodSource("invalidDurations")
        void shouldRejectInvalidDurations(Duration invalidDuration, String expectedErrorMessage, int expectedErrorCode) {
            // When & Then
            assertThatExceptionOfType(OrderAccessDurationException.class)
                    .isThrownBy(() -> OrderAccessDuration.of(invalidDuration))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        private static Stream<Arguments> invalidDurations() {
            return Stream.of(
                    Arguments.of(
                            null,
                            OrderAccessDurationExceptionInfo.missingValue().getMessage(),
                            OrderAccessDurationExceptionInfo.missingValue().getDomainErrorCode()
                    ),
                    Arguments.of(
                            Duration.ZERO,
                            OrderAccessDurationExceptionInfo.mustBePositive().getMessage(),
                            OrderAccessDurationExceptionInfo.mustBePositive().getDomainErrorCode()
                    ),
                    Arguments.of(
                            Duration.ofDays(-1),
                            OrderAccessDurationExceptionInfo.mustBePositive().getMessage(),
                            OrderAccessDurationExceptionInfo.mustBePositive().getDomainErrorCode()
                    )
            );
        }
    }
}
