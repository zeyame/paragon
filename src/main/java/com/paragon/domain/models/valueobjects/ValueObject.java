package com.paragon.domain.models.valueobjects;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public abstract class ValueObject {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        ValueObject other = (ValueObject) obj;
        Iterator<Object> thisComponents = this.getEqualityComponents().iterator();
        Iterator<Object> otherComponents = other.getEqualityComponents().iterator();

        while (thisComponents.hasNext() && otherComponents.hasNext()) {
            if (!Objects.equals(thisComponents.next(), otherComponents.next())) {
                return false;
            }
        }
        return !thisComponents.hasNext() && !otherComponents.hasNext();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEqualityComponents().toArray());
    }

    protected abstract List<Object> getEqualityComponents();
}
