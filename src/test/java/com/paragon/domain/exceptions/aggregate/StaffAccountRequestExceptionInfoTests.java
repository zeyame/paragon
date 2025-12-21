package com.paragon.domain.exceptions.aggregate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountRequestExceptionInfoTests {
    @Test
    void submittedByRequired_shouldHaveExpectedCodeAndMessage() {
        StaffAccountRequestExceptionInfo info = StaffAccountRequestExceptionInfo.submittedByRequired();

        assertThat(info.getMessage()).isEqualTo("Submitted by staff account ID is required for submitting a request.");
        assertThat(info.getDomainErrorCode()).isEqualTo(30001);
    }

    @Test
    void requestTypeRequired_shouldHaveExpectedCodeAndMessage() {
        StaffAccountRequestExceptionInfo info = StaffAccountRequestExceptionInfo.requestTypeRequired();

        assertThat(info.getMessage()).isEqualTo("Request type is required for submitting a request.");
        assertThat(info.getDomainErrorCode()).isEqualTo(30002);
    }

    @Test
    void targetIdAndTypeMustBeBothProvidedOrBothNull_shouldHaveExpectedCodeAndMessage() {
        StaffAccountRequestExceptionInfo info = StaffAccountRequestExceptionInfo.targetIdAndTypeMustBeBothProvidedOrBothNull();

        assertThat(info.getMessage()).isEqualTo("Target ID and target type must both be provided or both be null.");
        assertThat(info.getDomainErrorCode()).isEqualTo(30003);
    }

    @Test
    void pendingRequestAlreadyExistsForSubmitter_shouldHaveExpectedCodeAndMessage() {
        String requestType = "PASSWORD_CHANGE";
        StaffAccountRequestExceptionInfo info = StaffAccountRequestExceptionInfo.pendingRequestAlreadyExistsForSubmitter(requestType);

        assertThat(info.getMessage()).isEqualTo("A pending request of type 'PASSWORD_CHANGE' already exists.");
        assertThat(info.getDomainErrorCode()).isEqualTo(30004);
    }
}