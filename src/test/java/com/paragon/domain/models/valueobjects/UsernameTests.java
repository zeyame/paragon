package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.UsernameException;
import com.paragon.domain.exceptions.valueobject.UsernameExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class UsernameTests {

    @Nested
    class Of {
        @ParameterizedTest
        @ValueSource(strings = {"john_doe", "Alice123", "RayHunter"})
        void shouldCreateValidUsernames(String value) {
            // When
            Username username = Username.of(value);

            // Then
            assertThat(username.getValue())
                    .isNotNull()
                    .isEqualTo(value);
        }

        @ParameterizedTest
        @MethodSource("invalidUsernames")
        void shouldRejectInvalidUsernames(String invalidUsername, String expectedErrorMessage, int expectedErrorCode) {
            // When & Then
            assertThatExceptionOfType(UsernameException.class)
                    .isThrownBy(() -> Username.of(invalidUsername))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        private static Stream<Arguments> invalidUsernames() {
            return Stream.of(
                    Arguments.of(null, UsernameExceptionInfo.mustNotBeBlank().getMessage(), 103001),
                    Arguments.of("", UsernameExceptionInfo.mustNotBeBlank().getMessage(), 103001),
                    Arguments.of("ab", UsernameExceptionInfo.lengthOutOfRange().getMessage(), 103002),
                    Arguments.of("thisusernameistoolongforme", UsernameExceptionInfo.lengthOutOfRange().getMessage(), 103002),
                    Arguments.of("inv@lid", UsernameExceptionInfo.invalidCharacters().getMessage(), 103003),
                    Arguments.of("double__underscores", UsernameExceptionInfo.consecutiveUnderscores().getMessage(), 103004),
                    Arguments.of("1username", UsernameExceptionInfo.mustStartWithALetter().getMessage(), 103005),
                    Arguments.of("username_", UsernameExceptionInfo.mustNotEndWithUnderscore().getMessage(), 103006),
                    Arguments.of("admin", UsernameExceptionInfo.reservedWord().getMessage(), 103007)
            );
        }
    }
}
