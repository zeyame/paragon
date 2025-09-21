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
}
