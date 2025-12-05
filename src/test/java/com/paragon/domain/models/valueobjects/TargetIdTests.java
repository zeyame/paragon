package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.TargetIdException;
import com.paragon.domain.exceptions.valueobject.TargetIdExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TargetIdTests {
    @Nested
    class Of {
        @Test
        void givenValidStringId_whenOfIsCalled_thenReturnsValidAuditEntryId() {
            // Given
            String id = "target-id-can-be-of-any-format";

            // When & Then
            assertThat(TargetId.of(id).getValue())
                    .isEqualTo(id);
        }

        @ParameterizedTest
        @MethodSource("invalidInputs")
        void givenNullOrEmptyId_whenOfIsCalled_thenThrowsTargetIdException(String invalidId, TargetIdExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(TargetIdException.class)
                    .isThrownBy(() -> TargetId.of(invalidId))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> invalidInputs() {
            return Stream.of(
                    Arguments.of(null, TargetIdExceptionInfo.missingValue()),
                    Arguments.of("", TargetIdExceptionInfo.missingValue())
            );
        }
    }
}
