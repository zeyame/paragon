package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PasswordException;
import com.paragon.domain.exceptions.valueobject.PasswordExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class PasswordTests {
    @Nested
    class Of {
        @Test
        void shouldCreatePasswordFromValidHash() {
            // Given
            String validHash = "$argon2id$v=19$m=65536,t=3,p=1$QWxhZGRpbjpPcGVuU2VzYW1l$2iYvT1yzFzHtXJH7zM4jW1Z2sK7Tg==";

            // When
            Password password = Password.of(validHash);

            // Then
            assertThat(password.getValue()).isEqualTo(validHash);
        }

        @ParameterizedTest
        @MethodSource("invalidPasswords")
        void shouldRejectInvalidPasswords(String invalidPassword, String expectedErrorMessage, int expectedErrorCode) {
            // When & Then
            assertThatExceptionOfType(PasswordException.class)
                    .isThrownBy(() -> Password.of(invalidPassword))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        private static Stream<Arguments> invalidPasswords() {
            return Stream.of(
                    Arguments.of(null, PasswordExceptionInfo.missingValue().getMessage(), 105001),
                    Arguments.of("", PasswordExceptionInfo.missingValue().getMessage(), 105001)
            );
        }
    }
}
