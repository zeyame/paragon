package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.AuditEntryIdException;
import com.paragon.domain.exceptions.valueobject.AuditEntryIdExceptionInfo;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class AuditEntryId extends ValueObject {
    private final UUID value;

    private AuditEntryId(UUID value) {
        this.value = value;
    }

    public static AuditEntryId from(String value) {
        assertValidAuditTrailEntryId(value);
        return new AuditEntryId(UUID.fromString(value));
    }

    public static AuditEntryId generate() {
        return new AuditEntryId(UUID.randomUUID());
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidAuditTrailEntryId(String value) {
        if (value == null || value.isEmpty()) {
            throw new AuditEntryIdException(AuditEntryIdExceptionInfo.missingValue());
        }
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new AuditEntryIdException(AuditEntryIdExceptionInfo.invalidFormat());
        }
    }
}
