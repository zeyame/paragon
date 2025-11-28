package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.DateTimeUtcException;
import com.paragon.domain.exceptions.valueobject.DateTimeUtcExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

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
                    .isThrownBy(() -> DateTimeUtc.from((String) null))
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

    @Nested
    class IsBefore {
        @Test
        void shouldReturnTrue_whenValueIsBeforeOther() {
            DateTimeUtc earlier = DateTimeUtc.from("2024-01-01T00:00:00Z");
            DateTimeUtc later = DateTimeUtc.from("2024-01-02T00:00:00Z");

            assertThat(earlier.isBefore(later)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenValueIsNotBeforeOther() {
            DateTimeUtc earlier = DateTimeUtc.from("2024-01-01T00:00:00Z");
            DateTimeUtc later = DateTimeUtc.from("2024-01-02T00:00:00Z");

            assertThat(later.isBefore(earlier)).isFalse();
        }

        @Test
        void shouldThrowException_whenOtherIsNull() {
            DateTimeUtc dateTimeUtc = DateTimeUtc.from("2024-01-01T00:00:00Z");

            assertThatExceptionOfType(DateTimeUtcException.class)
                    .isThrownBy(() -> dateTimeUtc.isBefore(null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(
                            DateTimeUtcExceptionInfo.missingValue().getMessage(),
                            DateTimeUtcExceptionInfo.missingValue().getDomainErrorCode()
                    );
        }
    }

    @Nested
    class IsAfter {
        @Test
        void shouldReturnTrue_whenValueIsAfterOther() {
            DateTimeUtc earlier = DateTimeUtc.from("2024-01-01T00:00:00Z");
            DateTimeUtc later = DateTimeUtc.from("2024-01-02T00:00:00Z");

            assertThat(later.isAfter(earlier)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenValueIsNotAfterOther() {
            DateTimeUtc earlier = DateTimeUtc.from("2024-01-01T00:00:00Z");
            DateTimeUtc later = DateTimeUtc.from("2024-01-02T00:00:00Z");

            assertThat(earlier.isAfter(later)).isFalse();
        }

        @Test
        void shouldThrowException_whenOtherIsNull() {
            DateTimeUtc dateTimeUtc = DateTimeUtc.from("2024-01-01T00:00:00Z");

            assertThatExceptionOfType(DateTimeUtcException.class)
                    .isThrownBy(() -> dateTimeUtc.isAfter(null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(
                            DateTimeUtcExceptionInfo.missingValue().getMessage(),
                            DateTimeUtcExceptionInfo.missingValue().getDomainErrorCode()
                    );
        }
    }
}
