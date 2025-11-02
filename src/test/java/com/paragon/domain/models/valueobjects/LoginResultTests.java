package com.paragon.domain.models.valueobjects;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class LoginResultTests {
    @Nested
    class OfSuccess {
        @Test
        void shouldCreateSuccessfulLoginResult() {
            // When
            LoginResult result = LoginResult.ofSuccess();

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.failureReason()).isNull();
        }

        @Test
        void shouldIndicateSuccess() {
            // Given
            LoginResult result = LoginResult.ofSuccess();

            // When & Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailed()).isFalse();
        }
    }

    @Nested
    class OfFailure {
        @Test
        void shouldCreateFailedLoginResult() {
            // When
            LoginResult result = LoginResult.ofFailure();

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.failureReason()).isEqualTo("Invalid credentials");
        }

        @Test
        void shouldIndicateFailure() {
            // Given
            LoginResult result = LoginResult.ofFailure();

            // When & Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isFailed()).isTrue();
        }
    }
}