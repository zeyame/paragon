package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PlaintextPasswordException;
import com.paragon.domain.exceptions.valueobject.PlaintextPasswordExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class PlaintextPasswordTests {
    @Nested
    class Generate {
        @Test
        void shouldGeneratePasswordMeetingAllRequirements() {
            // When
            PlaintextPassword plaintextPassword = PlaintextPassword.generate();
            String password = plaintextPassword.getValue();

            // Then
            assertThat(password).hasSize(12);
            assertThat(password.chars().anyMatch(Character::isUpperCase))
                    .as("Password should contain at least one uppercase letter")
                    .isTrue();
            assertThat(password.chars().anyMatch(Character::isLowerCase))
                    .as("Password should contain at least one lowercase letter")
                    .isTrue();
            assertThat(password.chars().anyMatch(Character::isDigit))
                    .as("Password should contain at least one digit")
                    .isTrue();

            String specialCharacters = "!@#$%^&*()_+-=[]{}|;:,.<>?";
            assertThat(password.chars().anyMatch(ch -> specialCharacters.indexOf(ch) >= 0))
                    .as("Password should contain at least one special character")
                    .isTrue();
            assertThat(password.chars().noneMatch(Character::isWhitespace))
                    .as("Password should not contain any whitespace")
                    .isTrue();
        }

        @RepeatedTest(10)
        void shouldGenerateUniquePasswordsOnMultipleInvocations() {
            // When
            PlaintextPassword plaintextPassword = PlaintextPassword.generate();
            String password = plaintextPassword.getValue();

            // Then
            assertThat(password).hasSize(12);
            assertThat(password.chars().anyMatch(Character::isUpperCase)).isTrue();
            assertThat(password.chars().anyMatch(Character::isLowerCase)).isTrue();
            assertThat(password.chars().anyMatch(Character::isDigit)).isTrue();

            String specialCharacters = "!@#$%^&*()_+-=[]{}|;:,.<>?";
            assertThat(password.chars().anyMatch(ch -> specialCharacters.indexOf(ch) >= 0)).isTrue();
            assertThat(password.chars().noneMatch(Character::isWhitespace)).isTrue();
        }

        @Test
        void shouldGenerateUniquePasswords() {
            // Given
            Set<String> generatedPasswords = new HashSet<>();
            int numberOfPasswords = 100;

            // When
            for (int i = 0; i < numberOfPasswords; i++) {
                generatedPasswords.add(PlaintextPassword.generate().getValue());
            }

            // Then
            assertThat(generatedPasswords)
                    .as("All generated passwords should be unique")
                    .hasSize(numberOfPasswords);
        }
    }

    @Nested
    class Of {
        @ParameterizedTest
        @ValueSource(strings = {
                "ValidPass1!",
                "Another@Valid2",
                "StrongPassword123#",
                "MyP@ssw0rd"
        })
        void shouldCreateValidPlaintextPasswords(String value) {
            // When
            PlaintextPassword plaintextPassword = PlaintextPassword.of(value);

            // Then
            assertThat(plaintextPassword.getValue())
                    .isNotNull()
                    .isEqualTo(value);
        }

        @ParameterizedTest
        @MethodSource("invalidPlaintextPasswords")
        void shouldRejectInvalidPlaintextPasswords(String invalidPassword, PlaintextPasswordExceptionInfo exceptionInfo) {
            // When & Then
            assertThatExceptionOfType(PlaintextPasswordException.class)
                    .isThrownBy(() -> PlaintextPassword.of(invalidPassword))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(exceptionInfo.getMessage(), exceptionInfo.getDomainErrorCode());
        }

        private static Stream<Arguments> invalidPlaintextPasswords() {
            return Stream.of(
                    Arguments.of(null, PlaintextPasswordExceptionInfo.missingValue()),
                    Arguments.of("", PlaintextPasswordExceptionInfo.missingValue()),
                    Arguments.of("   ", PlaintextPasswordExceptionInfo.missingValue()),
                    Arguments.of("Short1!", PlaintextPasswordExceptionInfo.tooShort(8)),
                    Arguments.of("a".repeat(129) + "A1!", PlaintextPasswordExceptionInfo.tooLong(128)),
                    Arguments.of("alllowercase1!", PlaintextPasswordExceptionInfo.missingUppercase()),
                    Arguments.of("ALLUPPERCASE1!", PlaintextPasswordExceptionInfo.missingLowercase()),
                    Arguments.of("NoDigitsHere!", PlaintextPasswordExceptionInfo.missingDigit()),
                    Arguments.of("NoSpecial123", PlaintextPasswordExceptionInfo.missingSpecialCharacter()),
                    Arguments.of("Has Space1!", PlaintextPasswordExceptionInfo.containsWhitespace())
            );
        }
    }
}