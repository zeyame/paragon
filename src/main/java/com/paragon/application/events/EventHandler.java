package com.paragon.application.events;

import com.paragon.domain.events.DomainEvent;

public interface EventHandler<T extends DomainEvent> {
    void handle(T event);
    String subscribedToEventName();
}
