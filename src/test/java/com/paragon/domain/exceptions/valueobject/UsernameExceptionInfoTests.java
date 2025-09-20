package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UsernameExceptionInfoTests {

    @Test
    void missingValue_shouldHaveExpectedCodeAndMessage() {
        UsernameExceptionInfo info = UsernameExceptionInfo.missingValue();

        assertThat(info.getMessage()).isEqualTo("Username cannot be null or empty.");
        assertThat(info.getDomainErrorCode()).isEqualTo(103001);
    }

    @Test
    void lengthOutOfRange_shouldHaveExpectedCodeAndMessage() {
        UsernameExceptionInfo info = UsernameExceptionInfo.lengthOutOfRange();

        assertThat(info.getMessage()).isEqualTo("Username must be between 3 and 20 characters long.");
        assertThat(info.getDomainErrorCode()).isEqualTo(103002);
    }

    @Test
    void invalidCharacters_shouldHaveExpectedCodeAndMessage() {
        UsernameExceptionInfo info = UsernameExceptionInfo.invalidCharacters();

        assertThat(info.getMessage()).isEqualTo("Username can only contain letters, numbers, and underscores.");
        assertThat(info.getDomainErrorCode()).isEqualTo(103003);
    }

    @Test
    void consecutiveUnderscores_shouldHaveExpectedCodeAndMessage() {
        UsernameExceptionInfo info = UsernameExceptionInfo.consecutiveUnderscores();

        assertThat(info.getMessage()).isEqualTo("Username cannot contain consecutive underscores.");
        assertThat(info.getDomainErrorCode()).isEqualTo(103004);
    }

    @Test
    void mustStartWithALetter_shouldHaveExpectedCodeAndMessage() {
        UsernameExceptionInfo info = UsernameExceptionInfo.mustStartWithALetter();

        assertThat(info.getMessage()).isEqualTo("Username must start with a letter.");
        assertThat(info.getDomainErrorCode()).isEqualTo(103005);
    }

    @Test
    void mustNotEndWithUnderscore_shouldHaveExpectedCodeAndMessage() {
        UsernameExceptionInfo info = UsernameExceptionInfo.mustNotEndWithUnderscore();

        assertThat(info.getMessage()).isEqualTo("Username must not end with an underscore.");
        assertThat(info.getDomainErrorCode()).isEqualTo(103006);
    }

    @Test
    void reservedWord_shouldHaveExpectedCodeAndMessage() {
        UsernameExceptionInfo info = UsernameExceptionInfo.reservedWord();

        assertThat(info.getMessage()).isEqualTo("This is a reserved word and cannot be assigned as a username.");
        assertThat(info.getDomainErrorCode()).isEqualTo(103007);
    }
}
