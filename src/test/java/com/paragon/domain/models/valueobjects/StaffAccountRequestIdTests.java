package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.StaffAccountRequestIdException;
import com.paragon.domain.exceptions.valueobject.StaffAccountRequestIdExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class StaffAccountRequestIdTests {
    @Nested
    class Of {
        @Test
        void givenValidUUID_whenOfIsCalled_thenReturnsValidStaffAccountRequestId() {
            // Given
            UUID expectedUuid = UUID.randomUUID();

            // When & Then
            assertThat(StaffAccountRequestId.of(expectedUuid).getValue())
                    .isEqualTo(expectedUuid);
        }

        @Test
        void givenSameUUID_whenOfIsCalledTwice_thenIdsAreEqual() {
            UUID uuid = UUID.randomUUID();
            StaffAccountRequestId staffAccountRequestId1 = StaffAccountRequestId.of(uuid);
            StaffAccountRequestId staffAccountRequestId2 = StaffAccountRequestId.of(uuid);

            assertThat(staffAccountRequestId1).isEqualTo(staffAccountRequestId2);
        }

        @Test
        void givenNullId_whenOfIsCalled_thenThrowsStaffAccountRequestIdException() {
            // Given
            String expectedErrorMessage = StaffAccountRequestIdExceptionInfo.missingValue().getMessage();
            int expectedErrorCode = StaffAccountRequestIdExceptionInfo.missingValue().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountRequestIdException.class)
                    .isThrownBy(() -> StaffAccountRequestId.of(null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }

    @Nested
    class From {
        @Test
        void givenValidStringId_whenFromIsCalled_thenReturnsValidStaffAccountRequestId() {
            // Given
            String validUuidString = "12345678-1234-1234-1234-123456789abc";
            UUID expectedUuid = UUID.fromString(validUuidString);

            // When & Then
            assertThat(StaffAccountRequestId.from(validUuidString).getValue())
                    .isEqualTo(expectedUuid);
        }

        @ParameterizedTest
        @MethodSource("invalidInputs")
        void givenInvalidStringId_whenFromIsCalled_thenThrowsStaffAccountRequestIdException(String invalidId, StaffAccountRequestIdExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(StaffAccountRequestIdException.class)
                    .isThrownBy(() -> StaffAccountRequestId.from(invalidId))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> invalidInputs() {
            return Stream.of(
                    Arguments.of(null, StaffAccountRequestIdExceptionInfo.missingValue()),
                    Arguments.of("", StaffAccountRequestIdExceptionInfo.missingValue()),
                    Arguments.of("invalid-format", StaffAccountRequestIdExceptionInfo.invalidFormat())
            );
        }
    }

    @Nested
    class Generate {
        @Test
        void whenGenerateIsCalled_thenReturnsValidStaffAccountRequestId() {
            // Given
            StaffAccountRequestId staffAccountRequestId = StaffAccountRequestId.generate();

            // When & Then
            assertThat(staffAccountRequestId.getValue().toString())
                    .isNotNull()
                    .hasSize(36);
        }
    }
}