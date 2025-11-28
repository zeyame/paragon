package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHistoryEntryExceptionInfoTests {

    @Test
    void missingStaffAccountId_shouldReturnExpectedInfo() {
        PasswordHistoryEntryExceptionInfo info = PasswordHistoryEntryExceptionInfo.missingStaffAccountId();

        assertThat(info.getMessage()).isEqualTo("Staff account ID is required for password history entries.");
        assertThat(info.getDomainErrorCode()).isEqualTo(116001);
    }

    @Test
    void missingHashedPassword_shouldReturnExpectedInfo() {
        PasswordHistoryEntryExceptionInfo info = PasswordHistoryEntryExceptionInfo.missingHashedPassword();

        assertThat(info.getMessage()).isEqualTo("Password history entries require a hashed password value.");
        assertThat(info.getDomainErrorCode()).isEqualTo(116002);
    }

    @Test
    void missingChangedAtTimestamp_shouldReturnExpectedInfo() {
        PasswordHistoryEntryExceptionInfo info = PasswordHistoryEntryExceptionInfo.missingChangedAtTimestamp();

        assertThat(info.getMessage()).isEqualTo("Password history entries require the timestamp when the password was changed.");
        assertThat(info.getDomainErrorCode()).isEqualTo(116003);
    }
}
