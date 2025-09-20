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
                    Arguments.of(
                            null,
                            UsernameExceptionInfo.missingValue().getMessage(),
                            UsernameExceptionInfo.missingValue().getDomainErrorCode()
                    ),
                    Arguments.of(
                            "",
                            UsernameExceptionInfo.missingValue().getMessage(),
                            UsernameExceptionInfo.missingValue().getDomainErrorCode()
                    ),
                    Arguments.of(
                            "ab",
                            UsernameExceptionInfo.lengthOutOfRange().getMessage(),
                            UsernameExceptionInfo.lengthOutOfRange().getDomainErrorCode()
                    ),
                    Arguments.of(
                            "thisusernameistoolongforme",
                            UsernameExceptionInfo.lengthOutOfRange().getMessage(),
                            UsernameExceptionInfo.lengthOutOfRange().getDomainErrorCode()
                    ),
                    Arguments.of(
                            "inv@lid",
                            UsernameExceptionInfo.invalidCharacters().getMessage(),
                            UsernameExceptionInfo.invalidCharacters().getDomainErrorCode()
                    ),
                    Arguments.of(
                            "double__underscores",
                            UsernameExceptionInfo.consecutiveUnderscores().getMessage(),
                            UsernameExceptionInfo.consecutiveUnderscores().getDomainErrorCode()
                    ),
                    Arguments.of(
                            "1username",
                            UsernameExceptionInfo.mustStartWithALetter().getMessage(),
                            UsernameExceptionInfo.mustStartWithALetter().getDomainErrorCode()
                    ),
                    Arguments.of(
                            "username_",
                            UsernameExceptionInfo.mustNotEndWithUnderscore().getMessage(),
                            UsernameExceptionInfo.mustNotEndWithUnderscore().getDomainErrorCode()
                    ),
                    Arguments.of(
                            "admin",
                            UsernameExceptionInfo.reservedWord().getMessage(),
                            UsernameExceptionInfo.reservedWord().getDomainErrorCode()
                    )
            );
        }
    }
}
