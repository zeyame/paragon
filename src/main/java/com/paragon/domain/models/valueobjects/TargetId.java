package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.TargetIdException;
import com.paragon.domain.exceptions.valueobject.TargetIdExceptionInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class TargetId extends ValueObject {
    private final String value;

    private TargetId(String value) {
        this.value = value;
    }

    public static TargetId of(String value) {
        assertValidAuditTrailTargetId(value);
        return new TargetId(value);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidAuditTrailTargetId(String value) {
        if (value == null || value.isBlank()) {
            throw new TargetIdException(TargetIdExceptionInfo.missingValue());
        }
    }
}
