package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.RefreshTokenHashException;
import com.paragon.domain.exceptions.valueobject.RefreshTokenHashExceptionInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class RefreshTokenHash extends ValueObject {
    private final String value;

    private RefreshTokenHash(String value) {
        this.value = value;
    }

    public static RefreshTokenHash of(String value) {
        if (value == null || value.isEmpty()) {
            throw new RefreshTokenHashException(RefreshTokenHashExceptionInfo.missingValue());
        }
        return new RefreshTokenHash(value);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }
}