package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.VersionException;
import com.paragon.domain.exceptions.valueobject.VersionExceptionInfo;

import java.util.List;

public class Version extends ValueObject {
    private final int value;

    private Version(int value) {
        assertVersionNumber(value);
        this.value = value;
    }

    public static Version initial() {
        return new Version(1);
    }

    public Version increase() {
        return new Version(value + 1);
    }

    public int getValue() {
        return value;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private void assertVersionNumber(int value) {
        if (value < 1) {
            throw new VersionException(VersionExceptionInfo.mustBeAtleastOne());
        }
    }
}
