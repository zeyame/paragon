package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.DateTimeUtcException;
import com.paragon.domain.exceptions.valueobject.DateTimeUtcExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DateTimeUtcTests {

    @Nested
    class Of {
        @Test
        void shouldCreateValueObject_whenInstantIsProvided() {
            // Given
            Instant instant = Instant.parse("2024-01-01T00:00:00Z");

            // When
            DateTimeUtc dateTimeUtc = DateTimeUtc.of(instant);

            // Then
            assertThat(dateTimeUtc.getValue()).isEqualTo(instant);
        }

        @Test
        void shouldThrowException_whenInstantIsNull() {
            assertThatExceptionOfType(DateTimeUtcException.class)
                    .isThrownBy(() -> DateTimeUtc.of(null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(
                            DateTimeUtcExceptionInfo.missingValue().getMessage(),
                            DateTimeUtcExceptionInfo.missingValue().getDomainErrorCode()
                    );
        }
    }

    @Nested
    class From {
        @ParameterizedTest
        @ValueSource(strings = {
                "2024-01-01T00:00:00Z",
                "2025-05-12T15:30:45.123Z"
        })
        void shouldParseIsoStrings(String isoString) {
            // When
            DateTimeUtc dateTimeUtc = DateTimeUtc.from(isoString);

            // Then
            assertThat(dateTimeUtc.getValue()).isEqualTo(Instant.parse(isoString));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "2024-13-01T00:00:00Z", "2024-01-01T00:00:00+01:00", "not-a-date"})
        void shouldThrowException_whenIsoStringInvalid(String value) {
            DateTimeUtcExceptionInfo expectedInfo = value.isBlank()
                    ? DateTimeUtcExceptionInfo.missingValue()
                    : DateTimeUtcExceptionInfo.invalidFormat();

            assertThatExceptionOfType(DateTimeUtcException.class)
                    .isThrownBy(() -> DateTimeUtc.from(value))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedInfo.getMessage(), expectedInfo.getDomainErrorCode());
        }

        @Test
        void shouldThrowException_whenIsoStringIsNull() {
            assertThatExceptionOfType(DateTimeUtcException.class)
                    .isThrownBy(() -> DateTimeUtc.from(null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(
                            DateTimeUtcExceptionInfo.missingValue().getMessage(),
                            DateTimeUtcExceptionInfo.missingValue().getDomainErrorCode()
                    );
        }
    }

    @Nested
    class Now {
        @Test
        void shouldCreateValueObjectWithCurrentInstant() {
            // When
            DateTimeUtc dateTimeUtc = DateTimeUtc.now();

            // Then
            assertThat(dateTimeUtc.getValue()).isNotNull();
        }
    }
}
