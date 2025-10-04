package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.OrderAccessDurationException;
import com.paragon.domain.exceptions.valueobject.OrderAccessDurationExceptionInfo;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

@Getter
public class OrderAccessDuration extends ValueObject {
    private final Duration value;

    private OrderAccessDuration(Duration value) {
        this.value = value;
    }

    public static OrderAccessDuration from(int value) {
        assertValidDuration(value);
        return new OrderAccessDuration(Duration.ofDays(value));
    }

    public long getValueInDays() {
        return value.toDays();
    }

    private static void assertValidDuration(int value) {
        if (value <= 0) {
            throw new OrderAccessDurationException(OrderAccessDurationExceptionInfo.mustBePositive());
        }
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }
}
