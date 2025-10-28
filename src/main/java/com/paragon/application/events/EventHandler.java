package com.paragon.application.events;

import com.paragon.domain.events.DomainEvent;

import java.util.List;

public interface EventHandler<T extends DomainEvent> {
    void handle(T event);
    List<String> subscribedToEvents();
}
