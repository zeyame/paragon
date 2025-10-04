package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.AuditEntryTargetIdException;
import com.paragon.domain.exceptions.valueobject.AuditEntryTargetIdExceptionInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class AuditEntryTargetId extends ValueObject {
    private final String value;

    private AuditEntryTargetId(String value) {
        this.value = value;
    }

    public static AuditEntryTargetId of(String value) {
        assertValidAuditTrailTargetId(value);
        return new AuditEntryTargetId(value);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidAuditTrailTargetId(String value) {
        if (value == null || value.isBlank()) {
            throw new AuditEntryTargetIdException(AuditEntryTargetIdExceptionInfo.missingValue());
        }
    }
}
