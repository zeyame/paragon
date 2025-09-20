package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.ModmailTranscriptAccessDurationException;
import com.paragon.domain.exceptions.valueobject.ModmailTranscriptAccessDurationExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ModmailTranscriptAccessDurationTests {
    @Nested
    class Of {
        @ParameterizedTest
        @ValueSource(ints = {3, 5, 10, 20, 28})
        void shouldCreateValidDuration(int validDuration) {
            // Given
            Duration duration = Duration.ofDays(validDuration);

            // When
            ModmailTranscriptAccessDuration accessDuration = ModmailTranscriptAccessDuration.of(duration);

            // Then
            assertThat(accessDuration.getValue()).isEqualTo(duration);
        }

        @ParameterizedTest
        @MethodSource("invalidDurations")
        void shouldRejectInvalidDurations(Duration invalidDuration, String expectedErrorMessage, int expectedErrorCode) {
            // When & Then
            assertThatExceptionOfType(ModmailTranscriptAccessDurationException.class)
                    .isThrownBy(() -> ModmailTranscriptAccessDuration.of(invalidDuration))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        private static Stream<Arguments> invalidDurations() {
            return Stream.of(
                    Arguments.of(null, ModmailTranscriptAccessDurationExceptionInfo.missingValue().getMessage(), 107001),
                    Arguments.of(Duration.ZERO, ModmailTranscriptAccessDurationExceptionInfo.mustBePositive().getMessage(), 107002),
                    Arguments.of(Duration.ofDays(-1), ModmailTranscriptAccessDurationExceptionInfo.mustBePositive().getMessage(), 107002)
            );
        }
    }
}
