package com.paragon.domain.models.aggregates;

import com.paragon.domain.models.valueobjects.Version;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

@Getter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSourcedAggregate<?, ?> that = (EventSourcedAggregate<?, ?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    protected void enqueue(TEvent event) {
        uncommittedEvents.offer(event);
    }

    protected void increaseVersion() {
        version = version.increase();
    }
}
