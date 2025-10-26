package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.FailedLoginAttemptsException;
import com.paragon.domain.exceptions.valueobject.FailedLoginAttemptsExceptionInfo;

import java.util.List;

public class FailedLoginAttempts extends ValueObject {
    private final int value;
    private final static int MAX_ATTEMPTS = 5;

    private FailedLoginAttempts(int value) {
        this.value = value;
    }

    public static FailedLoginAttempts of(int value) {
        assertValidLoginAttempts(value);
        return new FailedLoginAttempts(value);
    }

    public static FailedLoginAttempts zero() {
        return new FailedLoginAttempts(0);
    }

    public FailedLoginAttempts increment() {
        if (value >= MAX_ATTEMPTS) {
            throw new FailedLoginAttemptsException(FailedLoginAttemptsExceptionInfo.maxAttemptsReached());
        }
        return new FailedLoginAttempts(value + 1);
    }

    public FailedLoginAttempts reset() {
        return zero();
    }

    public boolean hasReachedMax() {
        return value == MAX_ATTEMPTS;
    }

    public int getValue() {
        return value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidLoginAttempts(int value) {
        if (value < 0 || value > MAX_ATTEMPTS) {
            throw new FailedLoginAttemptsException(FailedLoginAttemptsExceptionInfo.invalidAttemptNumber());
        }
    }
}
