package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PasswordException;
import com.paragon.domain.exceptions.valueobject.PasswordExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class PasswordTests {
    @Nested
    class Of {
        @ParameterizedTest
        @ValueSource(strings = {
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                "$2a$12$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNO",
                "some-hashed-password-value"
        })
        void shouldCreateValidPassword(String hashedValue) {
            // When
            Password password = Password.of(hashedValue);

            // Then
            assertThat(password.getValue())
                    .isNotNull()
                    .isEqualTo(hashedValue);
        }

        @ParameterizedTest
        @MethodSource("invalidPasswords")
        void shouldRejectInvalidPasswords(String invalidPassword, PasswordExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(PasswordException.class)
                    .isThrownBy(() -> Password.of(invalidPassword))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> invalidPasswords() {
            return Stream.of(
                    Arguments.of(null, PasswordExceptionInfo.missingValue()),
                    Arguments.of("", PasswordExceptionInfo.missingValue()),
                    Arguments.of("   ", PasswordExceptionInfo.missingValue())
            );
        }
    }
}