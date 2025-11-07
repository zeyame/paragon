package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaintextPasswordExceptionInfoTests {
    @Test
    void missingValue_shouldHaveCorrectMessageAndErrorCode() {
        // When
        PlaintextPasswordExceptionInfo result = PlaintextPasswordExceptionInfo.missingValue();

        // Then
        assertThat(result.getMessage()).isEqualTo("Plaintext password cannot be null or empty.");
        assertThat(result.getDomainErrorCode()).isEqualTo(113001);
    }

    @Test
    void tooShort_shouldHaveCorrectMessageAndErrorCode() {
        // When
        PlaintextPasswordExceptionInfo result = PlaintextPasswordExceptionInfo.tooShort(8);

        // Then
        assertThat(result.getMessage()).isEqualTo("Password must be at least 8 characters long.");
        assertThat(result.getDomainErrorCode()).isEqualTo(113002);
    }

    @Test
    void tooLong_shouldHaveCorrectMessageAndErrorCode() {
        // When
        PlaintextPasswordExceptionInfo result = PlaintextPasswordExceptionInfo.tooLong(128);

        // Then
        assertThat(result.getMessage()).isEqualTo("Password must not exceed 128 characters.");
        assertThat(result.getDomainErrorCode()).isEqualTo(113003);
    }

    @Test
    void missingUppercase_shouldHaveCorrectMessageAndErrorCode() {
        // When
        PlaintextPasswordExceptionInfo result = PlaintextPasswordExceptionInfo.missingUppercase();

        // Then
        assertThat(result.getMessage()).isEqualTo("Password must contain at least one uppercase letter.");
        assertThat(result.getDomainErrorCode()).isEqualTo(113004);
    }

    @Test
    void missingLowercase_shouldHaveCorrectMessageAndErrorCode() {
        // When
        PlaintextPasswordExceptionInfo result = PlaintextPasswordExceptionInfo.missingLowercase();

        // Then
        assertThat(result.getMessage()).isEqualTo("Password must contain at least one lowercase letter.");
        assertThat(result.getDomainErrorCode()).isEqualTo(113005);
    }

    @Test
    void missingDigit_shouldHaveCorrectMessageAndErrorCode() {
        // When
        PlaintextPasswordExceptionInfo result = PlaintextPasswordExceptionInfo.missingDigit();

        // Then
        assertThat(result.getMessage()).isEqualTo("Password must contain at least one digit.");
        assertThat(result.getDomainErrorCode()).isEqualTo(113006);
    }

    @Test
    void missingSpecialCharacter_shouldHaveCorrectMessageAndErrorCode() {
        // When
        PlaintextPasswordExceptionInfo result = PlaintextPasswordExceptionInfo.missingSpecialCharacter();

        // Then
        assertThat(result.getMessage()).isEqualTo("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?).");
        assertThat(result.getDomainErrorCode()).isEqualTo(113007);
    }

    @Test
    void containsWhitespace_shouldHaveCorrectMessageAndErrorCode() {
        // When
        PlaintextPasswordExceptionInfo result = PlaintextPasswordExceptionInfo.containsWhitespace();

        // Then
        assertThat(result.getMessage()).isEqualTo("Password must not contain whitespace characters.");
        assertThat(result.getDomainErrorCode()).isEqualTo(113008);
    }
}