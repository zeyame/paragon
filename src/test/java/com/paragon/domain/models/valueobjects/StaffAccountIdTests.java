package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.StaffAccountIdException;
import com.paragon.domain.exceptions.valueobject.StaffAccountIdExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class StaffAccountIdTests {
    @Nested
    class Of {
        @Test
        void givenValidUUID_whenOfIsCalled_thenReturnsValidStaffAccountId() {
            // Given
            UUID expectedUuid = UUID.randomUUID();

            // When & Then
            assertThat(StaffAccountId.of(expectedUuid).getValue())
                    .isEqualTo(expectedUuid);
        }

        @Test
        void givenSameUUID_whenOfIsCalledTwice_thenIdsAreEqual() {
            UUID uuid = UUID.randomUUID();
            StaffAccountId staffAccountId1 = StaffAccountId.of(uuid);
            StaffAccountId staffAccountId2 = StaffAccountId.of(uuid);

            assertThat(staffAccountId1).isEqualTo(staffAccountId2);
        }

        @Test
        void givenNullId_whenOfIsCalled_thenThrowsStaffAccountIdException() {
            // Given
            String expectedErrorMessage = StaffAccountIdExceptionInfo.missingValue().getMessage();
            int expectedErrorCode = StaffAccountIdExceptionInfo.missingValue().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountIdException.class)
                    .isThrownBy(() -> StaffAccountId.of(null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }

    @Nested
    class From {
        @Test
        void givenValidStringId_whenFromIsCalled_thenReturnsValidStaffAccountId() {
            // Given
            String validUuidString = "12345678-1234-1234-1234-123456789abc";
            UUID expectedUuid = UUID.fromString(validUuidString);

            // When & Then
            assertThat(StaffAccountId.from(validUuidString).getValue())
                    .isEqualTo(expectedUuid);
        }

        @ParameterizedTest
        @MethodSource("invalidInputs")
        void givenInvalidStringId_whenFromIsCalled_thenThrowsStaffAccountIdException(String invalidId, StaffAccountIdExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(StaffAccountIdException.class)
                    .isThrownBy(() -> StaffAccountId.from(invalidId))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> invalidInputs() {
            return Stream.of(
                    Arguments.of(null, StaffAccountIdExceptionInfo.missingValue()),
                    Arguments.of("", StaffAccountIdExceptionInfo.missingValue()),
                    Arguments.of("invalid-format", StaffAccountIdExceptionInfo.invalidFormat())
            );
        }
    }

    @Nested
    class Generate {
        @Test
        void whenGenerateIsCalled_thenReturnsValidStaffAccountId() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();

            // When & Then
            assertThat(staffAccountId.getValue().toString())
                    .isNotNull()
                    .hasSize(36);
        }
    }
}
