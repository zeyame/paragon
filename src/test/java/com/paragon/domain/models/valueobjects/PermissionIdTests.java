package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PermissionIdException;
import com.paragon.domain.exceptions.valueobject.PermissionIdExceptionInfo;
import com.paragon.domain.exceptions.valueobject.StaffAccountIdException;
import com.paragon.domain.exceptions.valueobject.StaffAccountIdExceptionInfo;
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
    class Of {
        @Test
        void givenValidUUID_whenOfIsCalled_thenReturnsValidPermissionId() {
            // Given
            UUID expectedUuid = UUID.randomUUID();

            // When & Then
            assertThat(PermissionId.of(expectedUuid).getValue())
                    .isEqualTo(expectedUuid);
        }

        @Test
        void givenSameUUID_whenOfIsCalledTwice_thenIdsAreEqual() {
            UUID uuid = UUID.randomUUID();
            PermissionId permissionId1 = PermissionId.of(uuid);
            PermissionId permissionId2 = PermissionId.of(uuid);

            assertThat(permissionId1).isEqualTo(permissionId2);
        }

        @Test
        void givenNullId_whenOfIsCalled_thenThrowsPermissionIdException() {
            // Given
            String expectedErrorMessage = PermissionIdExceptionInfo.missingValue().getMessage();
            int expectedErrorCode = PermissionIdExceptionInfo.missingValue().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(PermissionIdException.class)
                    .isThrownBy(() -> PermissionId.of(null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
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

}
