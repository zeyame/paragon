package com.paragon.domain.models.aggregates;

import com.paragon.domain.models.valueobjects.Version;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public abstract class EventSourcedAggregate<TEvent, TId> implements AggregateRoot {
    protected final TId id;
    protected Version version;
    private final Deque<TEvent> uncommittedEvents;

    protected EventSourcedAggregate(TId id) {
        this.id = id;
        this.version = Version.initial();
        this.uncommittedEvents = new ArrayDeque<>();
    }

    public List<TEvent> dequeueUncommittedEvents() {
        List<TEvent> dequeuedEvents = uncommittedEvents.stream().toList();
        uncommittedEvents.clear();
        return dequeuedEvents;
    }

    protected void enqueue(TEvent event) {
        uncommittedEvents.offer(event);
    }

    protected void increaseVersion() {
        version = version.increase();
    }

    public TId getId() {
        return id;
    }

    public Version getVersion() {
        return version;
    }
}
