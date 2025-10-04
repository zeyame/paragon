package com.paragon.domain.models.entities;

import lombok.Getter;

import java.util.Objects;

@Getter
public abstract class Entity<T> {
    private final T id;

    protected Entity(T id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
