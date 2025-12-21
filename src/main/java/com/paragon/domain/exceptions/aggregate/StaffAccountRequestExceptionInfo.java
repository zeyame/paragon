package com.paragon.domain.exceptions.aggregate;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class StaffAccountRequestExceptionInfo extends DomainExceptionInfo {
    private StaffAccountRequestExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static StaffAccountRequestExceptionInfo submittedByRequired() {
        return new StaffAccountRequestExceptionInfo(
                "Submitted by staff account ID is required for submitting a request.",
                30001
        );
    }

    public static StaffAccountRequestExceptionInfo requestTypeRequired() {
        return new StaffAccountRequestExceptionInfo(
                "Request type is required for submitting a request.",
                30002
        );
    }

    public static StaffAccountRequestExceptionInfo targetIdAndTypeMustBeBothProvidedOrBothNull() {
        return new StaffAccountRequestExceptionInfo(
                "Target ID and target type must both be provided or both be null.",
                30003
        );
    }

    public static StaffAccountRequestExceptionInfo pendingRequestAlreadyExistsForSubmitter(String requestType) {
        return new StaffAccountRequestExceptionInfo(
                String.format("A pending request of type '%s' already exists.", requestType),
                30004
        );
    }
}