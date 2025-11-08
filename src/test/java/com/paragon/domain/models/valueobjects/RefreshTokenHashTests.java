package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.RefreshTokenHashException;
import com.paragon.domain.exceptions.valueobject.RefreshTokenHashExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class RefreshTokenHashTests {
    @Nested
    class Of {
        @Test
        void shouldReturnRefreshTokenHash() {
            // Given
            String hashedValue = "already-hashed-token-value";

            // When
            RefreshTokenHash refreshTokenHash = RefreshTokenHash.of(hashedValue);

            // Then
            assertThat(refreshTokenHash.getValue()).isEqualTo(hashedValue);
        }

        @ParameterizedTest
        @MethodSource("invalidHashValues")
        void shouldThrowRefreshTokenHashException(String invalidHashedValue) {
            // Given
            String expectedErrorMessage = RefreshTokenHashExceptionInfo.missingValue().getMessage();
            int expectedErrorCode = RefreshTokenHashExceptionInfo.missingValue().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(RefreshTokenHashException.class)
                    .isThrownBy(() -> RefreshTokenHash.of(invalidHashedValue))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        private static Stream<Arguments> invalidHashValues() {
            return Stream.of(
                    Arguments.of((String) null),
                    Arguments.of("")
            );
        }
    }
}