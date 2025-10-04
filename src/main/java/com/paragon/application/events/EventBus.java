package com.paragon.application.events;

import com.paragon.domain.events.DomainEvent;

import java.util.List;

public interface EventBus {
    <T extends DomainEvent> void registerHandler(EventHandler<T> handler);
    void publishAll(List<DomainEvent> events);
}
