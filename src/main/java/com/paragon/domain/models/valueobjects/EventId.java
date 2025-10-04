package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.EventIdException;
import com.paragon.domain.exceptions.valueobject.EventIdExceptionInfo;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class EventId extends ValueObject {
    private final UUID value;

    private EventId(UUID value) {
        assertValidEventId(value);
        this.value = value;
    }

    public static EventId generate() {
        return new EventId(UUID.randomUUID());
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private void assertValidEventId(UUID value) {
        if (value == null || value.equals(new UUID(0L, 0L))) {
            throw new EventIdException(EventIdExceptionInfo.mustNotBeNull());
        }
    }
}
