package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PasswordException;
import com.paragon.domain.exceptions.valueobject.PasswordExceptionInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class Password extends ValueObject {
    private final String value;

    private Password(String hashedValue) {
        this.value = hashedValue;
    }

    public static Password of(String hashedValue) {
        if (hashedValue == null || hashedValue.isBlank()) {
            throw new PasswordException(PasswordExceptionInfo.missingValue());
        }
        return new Password(hashedValue);
    }

    public static Password fromHashed(String hashedValue) {
        return new Password(hashedValue);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }
}
