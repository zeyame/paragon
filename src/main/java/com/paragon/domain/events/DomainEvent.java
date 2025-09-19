package com.paragon.domain.events;

import com.paragon.domain.models.valueobjects.EventId;

public abstract class DomainEvent {
    protected final EventId eventId;
    protected final String eventName;

    protected DomainEvent(EventId eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
    }
}
