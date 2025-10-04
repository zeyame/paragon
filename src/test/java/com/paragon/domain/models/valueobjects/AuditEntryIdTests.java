package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.AuditEntryIdException;
import com.paragon.domain.exceptions.valueobject.AuditEntryIdExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AuditEntryIdTests {
    @Nested
    class From {
        @Test
        void givenValidStringId_whenFromIsCalled_thenReturnsValidAuditEntryId() {
            // Given
            String validUuidString = "12345678-1234-1234-1234-123456789abc";
            UUID expectedUuid = UUID.fromString(validUuidString);

            // When & Then
            assertThat(AuditEntryId.from(validUuidString).getValue())
                    .isEqualTo(expectedUuid);
        }

        @ParameterizedTest
        @MethodSource("invalidInputs")
        void givenInvalidStringId_whenFromIsCalled_thenThrowsAuditEntryIdException(String invalidId, AuditEntryIdExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(AuditEntryIdException.class)
                    .isThrownBy(() -> AuditEntryId.from(invalidId))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> invalidInputs() {
            return Stream.of(
                    Arguments.of(null, AuditEntryIdExceptionInfo.missingValue()),
                    Arguments.of("", AuditEntryIdExceptionInfo.missingValue()),
                    Arguments.of("invalid-format", AuditEntryIdExceptionInfo.invalidFormat())
            );
        }
    }

    @Nested
    class Generate {
        @Test
        void whenGenerateIsCalled_thenReturnsValidAuditEntryId() {
            // Given
            AuditEntryId auditEntryId = AuditEntryId.generate();

            // When & Then
            assertThat(auditEntryId.getValue().toString())
                    .isNotNull()
                    .hasSize(36);
        }
    }
}
