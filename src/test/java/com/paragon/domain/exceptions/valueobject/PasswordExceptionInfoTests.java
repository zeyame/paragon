package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        PasswordExceptionInfo info = PasswordExceptionInfo.missingValue();

        assertThat(info.getMessage()).isEqualTo("Password cannot be null or empty.");
        assertThat(info.getDomainErrorCode()).isEqualTo(105001);
    }

    @Test
    void tooShort_shouldHaveExpectedCodeAndMessage() {
        int minLength = 8;
        PasswordExceptionInfo info = PasswordExceptionInfo.tooShort(minLength);

        assertThat(info.getMessage()).isEqualTo("Password must be at least 8 characters long.");
        assertThat(info.getDomainErrorCode()).isEqualTo(105002);
    }

    @Test
    void tooLong_shouldHaveExpectedCodeAndMessage() {
        int maxLength = 128;
        PasswordExceptionInfo info = PasswordExceptionInfo.tooLong(maxLength);

        assertThat(info.getMessage()).isEqualTo("Password must not exceed 128 characters.");
        assertThat(info.getDomainErrorCode()).isEqualTo(105003);
    }

    @Test
    void missingUppercase_shouldHaveExpectedCodeAndMessage() {
        PasswordExceptionInfo info = PasswordExceptionInfo.missingUppercase();

        assertThat(info.getMessage()).isEqualTo("Password must contain at least one uppercase letter.");
        assertThat(info.getDomainErrorCode()).isEqualTo(105004);
    }

    @Test
    void missingLowercase_shouldHaveExpectedCodeAndMessage() {
        PasswordExceptionInfo info = PasswordExceptionInfo.missingLowercase();

        assertThat(info.getMessage()).isEqualTo("Password must contain at least one lowercase letter.");
        assertThat(info.getDomainErrorCode()).isEqualTo(105005);
    }

    @Test
    void missingDigit_shouldHaveExpectedCodeAndMessage() {
        PasswordExceptionInfo info = PasswordExceptionInfo.missingDigit();

        assertThat(info.getMessage()).isEqualTo("Password must contain at least one digit.");
        assertThat(info.getDomainErrorCode()).isEqualTo(105006);
    }

    @Test
    void missingSpecialCharacter_shouldHaveExpectedCodeAndMessage() {
        PasswordExceptionInfo info = PasswordExceptionInfo.missingSpecialCharacter();

        assertThat(info.getMessage()).isEqualTo("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?).");
        assertThat(info.getDomainErrorCode()).isEqualTo(105007);
    }

    @Test
    void containsWhitespace_shouldHaveExpectedCodeAndMessage() {
        PasswordExceptionInfo info = PasswordExceptionInfo.containsWhitespace();

        assertThat(info.getMessage()).isEqualTo("Password must not contain whitespace characters.");
        assertThat(info.getDomainErrorCode()).isEqualTo(105008);
    }
}
