package com.paragon.application.events;

import com.paragon.domain.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventBusImpl implements EventBus {
    private static final Map<String, List<EventHandler<?>>> handlers = new HashMap<>();
    @Override
    public <T extends DomainEvent> void registerHandler(EventHandler<T> eventHandler) {
        handlers.computeIfAbsent(eventHandler.subscribedToEventName(), k -> new ArrayList<>())
                .add(eventHandler);
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            List<EventHandler<?>> eventHandlers = handlers.getOrDefault(event.getEventName(), List.of());
            for (EventHandler<?> handler : eventHandlers) {
                invoke(handler, event);
            }
        }
    }

    private <T extends DomainEvent> void invoke(EventHandler<T> handler, DomainEvent event) {
        @SuppressWarnings("unchecked")
        T typedEvent = (T) event;
        handler.handle(typedEvent);
    }
}
