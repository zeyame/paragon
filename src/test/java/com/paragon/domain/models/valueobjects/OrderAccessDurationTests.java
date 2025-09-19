package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.OrderAccessDurationException;
import com.paragon.domain.exceptions.valueobject.OrderAccessDurationExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.stream.Stream;

public class OrderAccessDurationTests {
    @Nested
    class Of {
        @Test
        void shouldCreateValidDuration() {
            // Given
            Duration duration = Duration.ofDays(3);

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
                    Arguments.of(null, OrderAccessDurationExceptionInfo.missingValue().getMessage(), 106001),
                    Arguments.of(Duration.ZERO, OrderAccessDurationExceptionInfo.mustBePositive().getMessage(), 106002),
                    Arguments.of(Duration.ofDays(-1), OrderAccessDurationExceptionInfo.mustBePositive().getMessage(), 106002),
                    Arguments.of(Duration.ofDays(31), OrderAccessDurationExceptionInfo.exceedsMaximum().getMessage(), 106003)
            );
        }
    }
}
