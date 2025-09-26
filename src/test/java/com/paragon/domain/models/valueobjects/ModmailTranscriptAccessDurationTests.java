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
            Duration expectedDuration = Duration.ofDays(validDuration);

            // When
            ModmailTranscriptAccessDuration accessDuration = ModmailTranscriptAccessDuration.from(validDuration);

            // Then
            assertThat(accessDuration.getValue()).isEqualTo(expectedDuration);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void shouldRejectInvalidDurations(int invalidDuration) {
            // Given
            String expectedErrorMessage = ModmailTranscriptAccessDurationExceptionInfo.mustBePositive().getMessage();
            int expectedErrorCode = ModmailTranscriptAccessDurationExceptionInfo.mustBePositive().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(ModmailTranscriptAccessDurationException.class)
                    .isThrownBy(() -> ModmailTranscriptAccessDuration.from(invalidDuration))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }
}
