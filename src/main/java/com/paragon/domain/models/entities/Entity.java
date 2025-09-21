package com.paragon.domain.models.entities;

import lombok.Getter;

@Getter
public abstract class Entity<T> {
    private final T id;

    protected Entity(T id) {
        this.id = id;
    }
}
