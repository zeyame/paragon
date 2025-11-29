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

    public static StaffAccountExceptionInfo createdByRequired() {
        return new StaffAccountExceptionInfo(
                "Every staff account must be created by an existing staff account. 'createdBy' cannot be null.",
                10005
        );
    }

    public static StaffAccountExceptionInfo atLeastOnePermissionRequired() {
        return new StaffAccountExceptionInfo(
                "At least one permission must be assigned to a staff account.",
                10006
        );
    }

    public static StaffAccountExceptionInfo loginFailedAccountDisabled() {
        return new StaffAccountExceptionInfo(
                "Login failed: This account has been disabled.",
                10007
        );
    }

    public static StaffAccountExceptionInfo loginFailedAccountLocked() {
        return new StaffAccountExceptionInfo(
                "Login failed: This account is temporarily locked due to multiple failed login attempts.",
                10008
        );
    }

    public static StaffAccountExceptionInfo invalidCredentials() {
        return new StaffAccountExceptionInfo(
                "Invalid username or password.",
                10009
        );
    }

    public static StaffAccountExceptionInfo accountAlreadyDisabled() {
        return new StaffAccountExceptionInfo(
                "Staff account is already disabled",
                10010
        );
    }

    public static StaffAccountExceptionInfo accountAlreadyEnabled() {
        return new StaffAccountExceptionInfo(
                "Staff account is already enabled",
                10011
        );
    }

    public static StaffAccountExceptionInfo passwordChangeRequiresActiveAccount() {
        return new StaffAccountExceptionInfo(
                "Password can only be changed while the staff account is active.",
                10012
        );
    }

    public static StaffAccountExceptionInfo temporaryPasswordChangeRequiresPendingState() {
        return new StaffAccountExceptionInfo(
                "Temporary password can only be completed while the account is pending a password change.",
                10013
        );
    }
}
