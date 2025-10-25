package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.RefreshTokenIdException;
import com.paragon.domain.exceptions.valueobject.RefreshTokenIdExceptionInfo;

import java.util.List;
import java.util.UUID;

public class RefreshTokenId extends ValueObject {
    private final UUID value;

    private RefreshTokenId(UUID value) {
        this.value = value;
    }

    public static RefreshTokenId of(UUID value) {
        if (value == null) {
            throw new RefreshTokenIdException(RefreshTokenIdExceptionInfo.missingValue());
        }
        return new RefreshTokenId(value);
    }

    public static RefreshTokenId from(String rawId) {
        assertValidRefreshTokenId(rawId);
        return new RefreshTokenId(UUID.fromString(rawId));
    }

    public static RefreshTokenId generate() {
        return new RefreshTokenId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidRefreshTokenId(String rawId) {
        if (rawId == null || rawId.isEmpty()) {
            throw new RefreshTokenIdException(RefreshTokenIdExceptionInfo.missingValue());
        }
        try {
            UUID.fromString(rawId);
        } catch (IllegalArgumentException e) {
            throw new RefreshTokenIdException(RefreshTokenIdExceptionInfo.invalidFormat());
        }
    }
}