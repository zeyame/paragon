package com.paragon.domain.exceptions.aggregate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountExceptionInfoTests {

    @Test
    void usernameRequired_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.usernameRequired();

        assertThat(info.getMessage()).isEqualTo("Username is required for registration.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10001);
    }

    @Test
    void passwordRequired_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.passwordRequired();

        assertThat(info.getMessage()).isEqualTo("Password is required for registration.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10002);
    }

    @Test
    void orderAccessDurationRequired_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.orderAccessDurationRequired();

        assertThat(info.getMessage()).isEqualTo("Order access duration is required for registration.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10003);
    }

    @Test
    void modmailTranscriptAccessDurationRequired_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.modmailTranscriptAccessDurationRequired();

        assertThat(info.getMessage()).isEqualTo("Modmail transcript access duration is required for registration.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10004);
    }

    @Test
    void createdByStaffAccountIdRequired_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.createdByRequired();

        assertThat(info.getMessage()).isEqualTo("Every staff account must be created by an existing staff account. 'createdBy' cannot be null.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10005);
    }

    @Test
    void atLeastOnePermissionRequired_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.atLeastOnePermissionRequired();

        assertThat(info.getMessage()).isEqualTo("At least one permission must be assigned to a staff account.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10006);
    }
}
