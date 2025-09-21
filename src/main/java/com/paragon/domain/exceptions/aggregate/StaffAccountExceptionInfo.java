package com.paragon.domain.exceptions.aggregate;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class StaffAccountExceptionInfo extends DomainExceptionInfo {
    private StaffAccountExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static StaffAccountExceptionInfo usernameRequired() {
        return new StaffAccountExceptionInfo(
                "Username is required for registration.",
                10001
        );
    }

    public static StaffAccountExceptionInfo passwordRequired() {
        return new StaffAccountExceptionInfo(
                "Password is required for registration.",
                10002
        );
    }

    public static StaffAccountExceptionInfo orderAccessDurationRequired() {
        return new StaffAccountExceptionInfo(
                "Order access duration is required for registration.",
                10003
        );
    }

    public static StaffAccountExceptionInfo modmailTranscriptAccessDurationRequired() {
        return new StaffAccountExceptionInfo(
                "Modmail transcript access duration is required for registration.",
                10004
        );
    }

    public static StaffAccountExceptionInfo atLeastOnePermissionRequired() {
        return new StaffAccountExceptionInfo(
                "At least one permission must be assigned to a staff account.",
                10005
        );
    }
}
