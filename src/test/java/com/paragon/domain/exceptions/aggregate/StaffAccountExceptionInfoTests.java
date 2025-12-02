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

    @Test
    void loginFailedAccountDisabled_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.loginFailedAccountDisabled();

        assertThat(info.getMessage()).isEqualTo("Login failed: This account has been disabled.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10007);
    }

    @Test
    void loginFailedAccountLocked_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.loginFailedAccountLocked();

        assertThat(info.getMessage()).isEqualTo("Login failed: This account is temporarily locked due to multiple failed login attempts.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10008);
    }

    @Test
    void invalidCredentials_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.invalidCredentials();

        assertThat(info.getMessage()).isEqualTo("Invalid username or password.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10009);
    }

    @Test
    void accountAlreadyDisabled_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.accountAlreadyDisabled();

        assertThat(info.getMessage()).isEqualTo("Staff account is already disabled");
        assertThat(info.getDomainErrorCode()).isEqualTo(10010);
    }

    @Test
    void accountAlreadyEnabled_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.accountAlreadyEnabled();

        assertThat(info.getMessage()).isEqualTo("Staff account is already enabled");
        assertThat(info.getDomainErrorCode()).isEqualTo(10011);
    }

    @Test
    void passwordChangeRequiresActiveAccount_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.passwordChangeRequiresActiveAccount();

        assertThat(info.getMessage()).isEqualTo("Password can only be changed while the staff account is active.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10012);
    }

    @Test
    void temporaryPasswordChangeRequiresPendingState_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.temporaryPasswordChangeRequiresPendingState();

        assertThat(info.getMessage()).isEqualTo("Temporary password can only be completed while the account is pending a password change.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10013);
    }

    @Test
    void passwordMustDifferFromCurrent_shouldHaveExpectedCodeAndMessage() {
        StaffAccountExceptionInfo info = StaffAccountExceptionInfo.passwordMustDifferFromCurrent();

        assertThat(info.getMessage()).isEqualTo("New password must be different from the current password.");
        assertThat(info.getDomainErrorCode()).isEqualTo(10014);
    }
}
