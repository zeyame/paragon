package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.EmailException;
import com.paragon.domain.exceptions.valueobject.EmailExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class EmailTests {

    @Nested
    class Of {
        @ParameterizedTest
        @ValueSource(strings = {"john.doe@example.com", "alice_123@my-domain.org", "jane-doe99@sub.example.co.uk"})
        void shouldCreateValidEmails(String validEmail) {
            // When
            Email email = Email.of(validEmail);

            // Then
            assertThat(email.getValue()).isEqualTo(validEmail);
        }

        @ParameterizedTest
        @MethodSource("invalidEmails")
        void shouldRejectInvalidEmails(String invalidEmail, String expectedErrorMessage, int expectedErrorCode) {
            // When & Then
            assertThatExceptionOfType(EmailException.class)
                    .isThrownBy(() -> Email.of(invalidEmail))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        private static Stream<Arguments> invalidEmails() {
            return Stream.of(
                    Arguments.of(null, EmailExceptionInfo.missingValue().getMessage(), 104001),
                    Arguments.of("", EmailExceptionInfo.missingValue().getMessage(), 104001),
                    Arguments.of("a".repeat(310) + "@example.com", EmailExceptionInfo.lengthOutOfRange().getMessage(), 104002),
                    Arguments.of("plainaddress", EmailExceptionInfo.invalidFormat().getMessage(), 104003),
                    Arguments.of("@no-local-part.com", EmailExceptionInfo.invalidFormat().getMessage(), 104003),
                    Arguments.of("no-at-symbol.com", EmailExceptionInfo.invalidFormat().getMessage(), 104003),
                    Arguments.of("space in@domain.com", EmailExceptionInfo.invalidFormat().getMessage(), 104003)
            );
        }
    }
}
