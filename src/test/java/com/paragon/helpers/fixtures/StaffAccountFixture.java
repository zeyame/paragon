package com.paragon.helpers.fixtures;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class StaffAccountFixture {
    private String id = UUID.randomUUID().toString();
    private String username = "testuser";
    private String email = null;
    private String password = "SecurePass123!";
    private boolean isPasswordTemporary = true;
    private Instant passwordIssuedAt = Instant.now();
    private int orderAccessDuration = 7;
    private int modmailTranscriptAccessDuration = 14;
    private StaffAccountStatus status = StaffAccountStatus.PENDING_PASSWORD_CHANGE;
    private int failedLoginAttempts = 0;
    private Instant lockedUntil = null;
    private Instant lastLoginAt = null;
    private String createdBy = UUID.randomUUID().toString();
    private String disabledBy = null;
    private String enabledBy = null;
    private String passwordResetBy = null;
    private List<String> permissionCodes = List.of("VIEW_LOGIN_LOGS", "VIEW_ACCOUNTS_LIST");
    private int version = 1;

    public StaffAccountFixture withId(String value) {
        this.id = value;
        return this;
    }

    public StaffAccountFixture withUsername(String value) {
        this.username = value;
        return this;
    }

    public StaffAccountFixture withEmail(String value) {
        this.email = value;
        return this;
    }

    public StaffAccountFixture withPassword(String value) {
        this.password = value;
        return this;
    }

    public StaffAccountFixture withPasswordTemporary(boolean value) {
        this.isPasswordTemporary = value;
        return this;
    }

    public StaffAccountFixture withPasswordIssuedAt(Instant value) {
        this.passwordIssuedAt = value;
        return this;
    }

    public StaffAccountFixture withOrderAccessDuration(int days) {
        this.orderAccessDuration = days;
        return this;
    }

    public StaffAccountFixture withModmailTranscriptAccessDuration(int days) {
        this.modmailTranscriptAccessDuration = days;
        return this;
    }

    public StaffAccountFixture withStatus(StaffAccountStatus value) {
        this.status = value;
        return this;
    }

    public StaffAccountFixture withFailedLoginAttempts(int attempts) {
        this.failedLoginAttempts = attempts;
        return this;
    }

    public StaffAccountFixture withLockedUntil(Instant value) {
        this.lockedUntil = value;
        return this;
    }

    public StaffAccountFixture withLastLoginAt(Instant value) {
        this.lastLoginAt = value;
        return this;
    }

    public StaffAccountFixture withCreatedBy(String value) {
        this.createdBy = value;
        return this;
    }

    public StaffAccountFixture withDisabledBy(String value) {
        this.disabledBy = value;
        return this;
    }

    public StaffAccountFixture withEnabledBy(String value) {
        this.enabledBy = value;
        return this;
    }

    public StaffAccountFixture withPasswordResetBy(String value) {
        this.passwordResetBy = value;
        return this;
    }

    public StaffAccountFixture withPermissionCodes(List<String> values) {
        this.permissionCodes = values;
        return this;
    }

    public StaffAccountFixture withVersion(int value) {
        this.version = value;
        return this;
    }

    public StaffAccount build() {
        return StaffAccount.createFrom(
                StaffAccountId.from(id),
                Username.of(username),
                email != null ? Email.of(email) : null,
                Password.fromHashed(password),
                isPasswordTemporary,
                passwordIssuedAt,
                OrderAccessDuration.from(orderAccessDuration),
                ModmailTranscriptAccessDuration.from(modmailTranscriptAccessDuration),
                status,
                FailedLoginAttempts.of(failedLoginAttempts),
                lockedUntil,
                lastLoginAt,
                StaffAccountId.from(createdBy),
                disabledBy != null ? StaffAccountId.from(disabledBy) : null,
                enabledBy != null ? StaffAccountId.from(enabledBy) : null,
                passwordResetBy != null ? StaffAccountId.from(passwordResetBy) : null,
                permissionCodes.stream().map(PermissionCode::of).toList(),
                Version.of(version)
        );
    }

    public static StaffAccount validStaffAccount() {
        return new StaffAccountFixture().build();
    }
}
