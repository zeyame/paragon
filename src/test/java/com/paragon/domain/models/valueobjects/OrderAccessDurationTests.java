package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.OrderAccessDurationException;
import com.paragon.domain.exceptions.valueobject.OrderAccessDurationExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.stream.Stream;

public class OrderAccessDurationTests {
    @Nested
    class from {
        @ParameterizedTest
        @ValueSource(ints = {3, 5, 10, 20, 28})
        void shouldCreateValidDuration(int validDuration) {
            // Given
            Duration expectedDuration = Duration.ofDays(validDuration);

            // When
            OrderAccessDuration orderAccessDuration = OrderAccessDuration.from(validDuration);

            // Then
            assertThat(orderAccessDuration.getValue()).isEqualTo(expectedDuration);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void shouldRejectInvalidDurations(int invalidDuration) {
            // Given
            String expectedErrorMessage = OrderAccessDurationExceptionInfo.mustBePositive().getMessage();
            int expectedErrorCode = OrderAccessDurationExceptionInfo.mustBePositive().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(OrderAccessDurationException.class)
                    .isThrownBy(() -> OrderAccessDuration.from(invalidDuration))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }
}
