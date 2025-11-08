package com.paragon.domain.exceptions.aggregate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenExceptionInfoTests {
    @Test
    void tokenHashRequired_shouldHaveExpectedCodeAndMessage() {
        // When
        RefreshTokenExceptionInfo exceptionInfo = RefreshTokenExceptionInfo.tokenHashRequired();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("A token hash is required to generate a refresh token.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(20001);
    }

    @Test
    void staffAccountIdRequired_shouldHaveExpectedCodeAndMessage() {
        // When
        RefreshTokenExceptionInfo exceptionInfo = RefreshTokenExceptionInfo.staffAccountIdRequired();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("A staff account id is required to generate a refresh token.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(20002);
    }

    @Test
    void tokenAlreadyRevoked_shouldHaveExpectedCodeAndMessage() {
        // When
        RefreshTokenExceptionInfo exceptionInfo = RefreshTokenExceptionInfo.tokenAlreadyRevoked();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Refresh token has already been revoked.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(20003);
    }

    @Test
    void ipAddressRequired_shouldHaveExpectedCodeAndMessage() {
        // When
        RefreshTokenExceptionInfo exceptionInfo = RefreshTokenExceptionInfo.ipAddressRequired();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("An IP address is required to issue a refresh token.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(20004);
    }
}
