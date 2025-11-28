package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.DateTimeUtcException;
import com.paragon.domain.exceptions.valueobject.DateTimeUtcExceptionInfo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DateTimeUtc extends ValueObject {
    private final Instant value;

    private DateTimeUtc(Instant value) {
        this.value = value;
    }

    public static DateTimeUtc of(Instant value) {
        if (value == null) {
            throw new DateTimeUtcException(DateTimeUtcExceptionInfo.missingValue());
        }
        return new DateTimeUtc(value);
    }

    public static DateTimeUtc from(String isoString) {
        if (isoString == null) {
            throw new DateTimeUtcException(DateTimeUtcExceptionInfo.missingValue());
        }

        String normalizedValue = isoString.trim();
        if (normalizedValue.isEmpty()) {
            throw new DateTimeUtcException(DateTimeUtcExceptionInfo.missingValue());
        }

        if (!normalizedValue.endsWith("Z")) {
            throw new DateTimeUtcException(DateTimeUtcExceptionInfo.invalidFormat());
        }

        try {
            return new DateTimeUtc(Instant.parse(normalizedValue));
        } catch (DateTimeParseException ex) {
            throw new DateTimeUtcException(DateTimeUtcExceptionInfo.invalidFormat());
        }
    }

    public static DateTimeUtc from(LocalDate date) {
        if (date == null) {
            throw new DateTimeUtcException(DateTimeUtcExceptionInfo.missingValue());
        }
        return new DateTimeUtc(date.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    public static DateTimeUtc now() {
        return new DateTimeUtc(Instant.now());
    }

    public Instant getValue() {
        return value;
    }

    public boolean isAfter(DateTimeUtc other) {
        assertOtherValuePresent(other);
        return value.isAfter(other.value);
    }

    public boolean isBefore(DateTimeUtc other) {
        assertOtherValuePresent(other);
        return value.isBefore(other.value);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private void assertOtherValuePresent(DateTimeUtc other) {
        if (other == null) {
            throw new DateTimeUtcException(DateTimeUtcExceptionInfo.missingValue());
        }
    }
}
