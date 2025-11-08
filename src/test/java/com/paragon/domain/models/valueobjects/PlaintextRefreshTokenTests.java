package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PlaintextRefreshTokenException;
import com.paragon.domain.exceptions.valueobject.PlaintextRefreshTokenExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class PlaintextRefreshTokenTests {
    @Nested
    class Generate {
        @Test
        void shouldGenerateTokenMeetingAllRequirements() {
            // When
            PlaintextRefreshToken plaintextRefreshToken = PlaintextRefreshToken.generate();
            String token = plaintextRefreshToken.getValue();

            // Then
            assertThat(token)
                    .as("Token should be exactly 64 characters")
                    .hasSize(64);
            assertThat(token)
                    .as("Token should be URL-safe (no padding, no special chars except - and _)")
                    .matches("^[A-Za-z0-9_-]+$");
        }

        @RepeatedTest(10)
        void shouldGenerateValidTokensOnMultipleInvocations() {
            // When
            PlaintextRefreshToken plaintextRefreshToken = PlaintextRefreshToken.generate();
            String token = plaintextRefreshToken.getValue();

            // Then
            assertThat(token).hasSize(64);
            assertThat(token).matches("^[A-Za-z0-9_-]+$");
        }

        @Test
        void shouldGenerateUniqueTokens() {
            // Given
            Set<String> generatedTokens = new HashSet<>();
            int numberOfTokens = 100;

            // When
            for (int i = 0; i < numberOfTokens; i++) {
                generatedTokens.add(PlaintextRefreshToken.generate().getValue());
            }

            // Then
            assertThat(generatedTokens)
                    .as("All generated tokens should be unique")
                    .hasSize(numberOfTokens);
        }
    }

    @Nested
    class Of {
        @ParameterizedTest
        @ValueSource(strings = {
                "validToken123",
                "another_valid-token",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
        })
        void shouldCreateValidPlaintextRefreshTokens(String value) {
            // When
            PlaintextRefreshToken plaintextRefreshToken = PlaintextRefreshToken.of(value);

            // Then
            assertThat(plaintextRefreshToken.getValue())
                    .isNotNull()
                    .isEqualTo(value);
        }

        @ParameterizedTest
        @MethodSource("invalidPlaintextRefreshTokens")
        void shouldRejectInvalidPlaintextRefreshTokens(String invalidToken, PlaintextRefreshTokenExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(PlaintextRefreshTokenException.class)
                    .isThrownBy(() -> PlaintextRefreshToken.of(invalidToken))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> invalidPlaintextRefreshTokens() {
            return Stream.of(
                    Arguments.of(null, PlaintextRefreshTokenExceptionInfo.missingValue()),
                    Arguments.of("", PlaintextRefreshTokenExceptionInfo.missingValue()),
                    Arguments.of("   ", PlaintextRefreshTokenExceptionInfo.missingValue())
            );
        }
    }
}