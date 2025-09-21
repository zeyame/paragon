package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PermissionIdException;
import com.paragon.domain.exceptions.valueobject.PermissionIdExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PermissionIdTests {
    @Nested
    class Generate {
        @Test
        void whenGenerateIsCalled_thenReturnsValidPermissionId() {
            // Given
            PermissionId permissionId = PermissionId.generate();

            // When & Then
            assertThat(permissionId.getValue().toString())
                    .isNotNull()
                    .hasSize(36);
        }
    }

    @Nested
    class From {
        @Test
        void givenValidStringId_whenFromIsCalled_thenReturnsValidPermissionId() {
            // Given
            String validUuidString = "12345678-1234-1234-1234-123456789abc";
            UUID expectedUuid = UUID.fromString(validUuidString);

            // When & Then
            assertThat(PermissionId.from(validUuidString).getValue()).isEqualTo(expectedUuid);
        }

        @ParameterizedTest
        @MethodSource("invalidInputs")
        void givenInvalidStringId_whenFromIsCalled_whenThrowsPermissionIdException(String invalidId, PermissionIdExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(PermissionIdException.class)
                    .isThrownBy(() -> PermissionId.from(invalidId))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> invalidInputs() {
            return Stream.of(
                    Arguments.of(null, PermissionIdExceptionInfo.missingValue()),
                    Arguments.of("", PermissionIdExceptionInfo.missingValue()),
                    Arguments.of("invalid-format", PermissionIdExceptionInfo.invalidFormat())
            );
        }
    }
}
