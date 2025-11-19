package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class DateTimeUtcExceptionInfo extends DomainExceptionInfo {
    private DateTimeUtcExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static DateTimeUtcExceptionInfo missingValue() {
        return new DateTimeUtcExceptionInfo(
                "Date/time value must not be null or blank.",
                115001
        );
    }

    public static DateTimeUtcExceptionInfo invalidFormat() {
        return new DateTimeUtcExceptionInfo(
                "Date/time value must be a valid ISO-8601 UTC timestamp (e.g. 2024-01-01T00:00:00Z).",
                115002
        );
    }
}
