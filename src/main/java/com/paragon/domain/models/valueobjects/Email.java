package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.EmailException;
import com.paragon.domain.exceptions.valueobject.EmailExceptionInfo;
import lombok.Getter;

import java.util.List;
import java.util.regex.Pattern;

@Getter
public class Email extends ValueObject {
    private final String value;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value) {
        assertValidEmail(value);
        return new Email(value);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidEmail(String value) {
        if (value == null || value.isBlank()) {
            throw new EmailException(EmailExceptionInfo.missingValue());
        }
        if (value.length() > 320) {
            throw new EmailException(EmailExceptionInfo.lengthOutOfRange());
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new EmailException(EmailExceptionInfo.invalidFormat());
        }
    }
}
