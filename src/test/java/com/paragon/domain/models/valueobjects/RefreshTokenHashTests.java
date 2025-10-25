package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.RefreshTokenHashException;
import com.paragon.domain.exceptions.valueobject.RefreshTokenHashExceptionInfo;
import com.paragon.domain.interfaces.TokenHasher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RefreshTokenHashTests {
    @Nested
    class FromPlainToken {
        @Test
        void givenValidPlainToken_whenFromPlainTokenIsCalled_thenHashesAndReturnsRefreshTokenHash() {
            // Given
            String plainToken = "my-plain-token-value";
            String expectedHash = "hashed-token-value";
            TokenHasher tokenHasher = mock(TokenHasher.class);
            when(tokenHasher.hash(plainToken)).thenReturn(expectedHash);

            // When
            RefreshTokenHash refreshTokenHash = RefreshTokenHash.fromPlainToken(plainToken, tokenHasher);

            // Then
            assertThat(refreshTokenHash.getValue()).isEqualTo(expectedHash);
            verify(tokenHasher, times(1)).hash(plainToken);
        }

        @ParameterizedTest
        @MethodSource("invalidTokens")
        void givenInvalidPlainToken_whenFromPlainTokenIsCalled_thenThrowsRefreshTokenHashException(String invalidToken) {
            // Given
            TokenHasher tokenHasher = mock(TokenHasher.class);
            String expectedErrorMessage = RefreshTokenHashExceptionInfo.missingValue().getMessage();
            int expectedErrorCode = RefreshTokenHashExceptionInfo.missingValue().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(RefreshTokenHashException.class)
                    .isThrownBy(() -> RefreshTokenHash.fromPlainToken(invalidToken, tokenHasher))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);

            verifyNoInteractions(tokenHasher);
        }

        private static Stream<Arguments> invalidTokens() {
            return Stream.of(
                    Arguments.of((String) null),
                    Arguments.of("")
            );
        }
    }

    @Nested
    class FromHashed {
        @Test
        void givenValidHashedValue_whenFromHashedIsCalled_thenReturnsRefreshTokenHash() {
            // Given
            String hashedValue = "already-hashed-token-value";

            // When
            RefreshTokenHash refreshTokenHash = RefreshTokenHash.fromHashed(hashedValue);

            // Then
            assertThat(refreshTokenHash.getValue()).isEqualTo(hashedValue);
        }

        @ParameterizedTest
        @MethodSource("invalidHashedValues")
        void givenInvalidHashedValue_whenFromHashedIsCalled_thenThrowsRefreshTokenHashException(String invalidHashedValue) {
            // Given
            String expectedErrorMessage = RefreshTokenHashExceptionInfo.missingValue().getMessage();
            int expectedErrorCode = RefreshTokenHashExceptionInfo.missingValue().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(RefreshTokenHashException.class)
                    .isThrownBy(() -> RefreshTokenHash.fromHashed(invalidHashedValue))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        private static Stream<Arguments> invalidHashedValues() {
            return Stream.of(
                    Arguments.of((String) null),
                    Arguments.of("")
            );
        }
    }

    @Nested
    class Equality {
        @Test
        void givenSameHashedValue_whenCompared_thenAreEqual() {
            // Given
            String hashedValue = "same-hash-value";
            RefreshTokenHash hash1 = RefreshTokenHash.fromHashed(hashedValue);
            RefreshTokenHash hash2 = RefreshTokenHash.fromHashed(hashedValue);

            // When & Then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        void givenDifferentHashedValues_whenCompared_thenAreNotEqual() {
            // Given
            RefreshTokenHash hash1 = RefreshTokenHash.fromHashed("hash-value-1");
            RefreshTokenHash hash2 = RefreshTokenHash.fromHashed("hash-value-2");

            // When & Then
            assertThat(hash1).isNotEqualTo(hash2);
        }
    }
}