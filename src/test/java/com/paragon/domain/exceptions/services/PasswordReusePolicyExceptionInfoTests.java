package com.paragon.domain.exceptions.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordReusePolicyExceptionInfoTests {

    @Test
    void passwordUsedWithinRestrictedWindow_shouldReturnExpectedInfo() {
        PasswordReusePolicyExceptionInfo info = PasswordReusePolicyExceptionInfo.passwordUsedWithinRestrictedWindow();

        assertThat(info.getMessage()).isEqualTo("The entered password was used recently and cannot be reused yet.");
        assertThat(info.getDomainErrorCode()).isEqualTo(300001);
    }
}
