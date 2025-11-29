package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StaffAccountPasswordHistoryExceptionInfoTests {

    @Test
    void mustContainEntries_shouldReturnExpectedInfo() {
        StaffAccountPasswordHistoryExceptionInfo info = StaffAccountPasswordHistoryExceptionInfo.mustContainEntries();

        assertThat(info.getMessage()).isEqualTo("Password history must contain at least one entry");
        assertThat(info.getDomainErrorCode()).isEqualTo(117001);
    }

    @Test
    void entriesMustBelongToSingleAccount_shouldReturnExpectedInfo() {
        StaffAccountPasswordHistoryExceptionInfo info = StaffAccountPasswordHistoryExceptionInfo.entriesMustBelongToSingleAccount();

        assertThat(info.getMessage()).isEqualTo("Password history entries must belong to the same staff account.");
        assertThat(info.getDomainErrorCode()).isEqualTo(117002);
    }
}
