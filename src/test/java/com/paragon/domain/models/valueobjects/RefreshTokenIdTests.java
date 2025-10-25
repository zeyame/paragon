package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.RefreshTokenIdException;
import com.paragon.domain.exceptions.valueobject.RefreshTokenIdExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class RefreshTokenIdTests {
    @Nested
    class Of {
        @Test
        void givenValidUUID_whenOfIsCalled_thenReturnsValidRefreshTokenId() {
            // Given
            UUID expectedUuid = UUID.randomUUID();

            // When & Then
            assertThat(RefreshTokenId.of(expectedUuid).getValue())
                    .isEqualTo(expectedUuid);
        }

        @Test
        void givenSameUUID_whenOfIsCalledTwice_thenIdsAreEqual() {
            UUID uuid = UUID.randomUUID();
            RefreshTokenId refreshTokenId1 = RefreshTokenId.of(uuid);
            RefreshTokenId refreshTokenId2 = RefreshTokenId.of(uuid);

            assertThat(refreshTokenId1).isEqualTo(refreshTokenId2);
        }

        @Test
        void givenNullId_whenOfIsCalled_thenThrowsRefreshTokenIdException() {
            // Given
            String expectedErrorMessage = RefreshTokenIdExceptionInfo.missingValue().getMessage();
            int expectedErrorCode = RefreshTokenIdExceptionInfo.missingValue().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(RefreshTokenIdException.class)
                    .isThrownBy(() -> RefreshTokenId.of(null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }

    @Nested
    class From {
        @Test
        void givenValidStringId_whenFromIsCalled_thenReturnsValidRefreshTokenId() {
            // Given
            String validUuidString = "12345678-1234-1234-1234-123456789abc";
            UUID expectedUuid = UUID.fromString(validUuidString);

            // When & Then
            assertThat(RefreshTokenId.from(validUuidString).getValue())
                    .isEqualTo(expectedUuid);
        }

        @ParameterizedTest
        @MethodSource("invalidInputs")
        void givenInvalidStringId_whenFromIsCalled_thenThrowsRefreshTokenIdException(String invalidId, RefreshTokenIdExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(RefreshTokenIdException.class)
                    .isThrownBy(() -> RefreshTokenId.from(invalidId))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> invalidInputs() {
            return Stream.of(
                    Arguments.of(null, RefreshTokenIdExceptionInfo.missingValue()),
                    Arguments.of("", RefreshTokenIdExceptionInfo.missingValue()),
                    Arguments.of("invalid-format", RefreshTokenIdExceptionInfo.invalidFormat())
            );
        }
    }

    @Nested
    class Generate {
        @Test
        void whenGenerateIsCalled_thenReturnsValidRefreshTokenId() {
            // Given
            RefreshTokenId refreshTokenId = RefreshTokenId.generate();

            // When & Then
            assertThat(refreshTokenId.getValue().toString())
                    .isNotNull()
                    .hasSize(36);
        }
    }
}