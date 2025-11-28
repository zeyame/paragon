package com.paragon.domain.exceptions.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StaffAccountPasswordReusePolicyExceptionInfoTests {

    @Test
    void passwordUsedWithinRestrictedWindow_shouldReturnExpectedInfo() {
        StaffAccountPasswordReusePolicyExceptionInfo info = StaffAccountPasswordReusePolicyExceptionInfo.passwordUsedWithinRestrictedWindow();

        assertThat(info.getMessage()).isEqualTo("The entered password was used recently and cannot be reused yet.");
        assertThat(info.getDomainErrorCode()).isEqualTo(300001);
    }
}
