package com.paragon.domain.models.aggregates;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountRegisteredEvent;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.*;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Getter
public class StaffAccount extends EventSourcedAggregate<DomainEvent, StaffAccountId> {

    private Username username;
    private Email email;
    private Password password;
    private boolean isPasswordTemporary;
    private Instant passwordIssuedAt;
    private OrderAccessDuration orderAccessDuration;
    private ModmailTranscriptAccessDuration modmailTranscriptAccessDuration;
    private StaffAccountStatus status;
    private FailedLoginAttempts failedLoginAttempts;
    private Instant lockedUntil;
    private Instant lastLoginAt;
    private final StaffAccountId createdBy;
    private StaffAccountId disabledBy;
    private final Set<PermissionCode> permissionCodes;

    private StaffAccount(StaffAccountId id,
                         Username username,
                         Email email,
                         Password password,
                         boolean isPasswordTemporary,
                         Instant passwordIssuedAt,
                         OrderAccessDuration orderAccessDuration,
                         ModmailTranscriptAccessDuration modmailTranscriptAccessDuration,
                         StaffAccountStatus status,
                         FailedLoginAttempts failedLoginAttempts,
                         Instant lockedUntil,
                         Instant lastLoginAt,
                         StaffAccountId createdBy,
                         StaffAccountId disabledBy,
                         Set<PermissionCode> permissionCodes,
                         Version version)
    {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.isPasswordTemporary = isPasswordTemporary;
        this.passwordIssuedAt = passwordIssuedAt;
        this.orderAccessDuration = orderAccessDuration;
        this.modmailTranscriptAccessDuration = modmailTranscriptAccessDuration;
        this.status = status;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.lastLoginAt = lastLoginAt;
        this.createdBy = createdBy;
        this.disabledBy = disabledBy;
        this.permissionCodes = permissionCodes;
        this.version = version;
    }

    public static StaffAccount register(Username username, Email email, Password password, OrderAccessDuration orderAccessDuration,
                                        ModmailTranscriptAccessDuration modmailTranscriptAccessDuration,
                                        StaffAccountId createdBy, Set<PermissionCode> permissionCodes)
    {
        assertValidRegistration(username, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionCodes);
        StaffAccount account = new StaffAccount(
                StaffAccountId.generate(), username, email, password, true, Instant.now(),
                orderAccessDuration, modmailTranscriptAccessDuration, StaffAccountStatus.PENDING_PASSWORD_CHANGE,
                FailedLoginAttempts.initial(), null, null, createdBy, null, permissionCodes, Version.initial()
        );
        account.enqueue(new StaffAccountRegisteredEvent(account));
        return account;
    }

    public void login(Password enteredPassword) {
        throwIfAccountIsDisabled();
        throwIfAccountIsLocked();
        authenticatePassword(enteredPassword);
        failedLoginAttempts = failedLoginAttempts.reset();
        lastLoginAt = Instant.now();
        increaseVersion();
    }

    public static StaffAccount createFrom(StaffAccountId id, Username username, Email email, Password password,
                                          boolean isPasswordTemporary, Instant passwordIssuedAt, OrderAccessDuration orderAccessDuration,
                                          ModmailTranscriptAccessDuration modmailTranscriptAccessDuration,
                                          StaffAccountStatus status, FailedLoginAttempts failedLoginAttempts,
                                          Instant lockedUntil, Instant lastLoginAt, StaffAccountId createdBy,
                                          StaffAccountId disabledBy, Set<PermissionCode> permissionCodes, Version version) {
        return new StaffAccount(
                id, username, email, password, isPasswordTemporary,
                passwordIssuedAt, orderAccessDuration,
                modmailTranscriptAccessDuration,
                status, failedLoginAttempts,
                lockedUntil, lastLoginAt,
                createdBy, disabledBy,
                permissionCodes, version
        );
    }

    public boolean canRegisterOtherStaffAccounts() {
        return permissionCodes.contains(SystemPermissions.MANAGE_ACCOUNTS);
    }

    public boolean requiresPasswordReset() {
        return isPasswordTemporary;
    }

    private static void assertValidRegistration(Username username, Password password, OrderAccessDuration orderAccessDuration,
                                                ModmailTranscriptAccessDuration modmailTranscriptAccessDuration, StaffAccountId createdBy,
                                                Set<PermissionCode> permissionCodes)
    {
        if (username == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.usernameRequired());
        }
        if (password == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.passwordRequired());
        }
        if (orderAccessDuration == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.orderAccessDurationRequired());
        }
        if (modmailTranscriptAccessDuration == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.modmailTranscriptAccessDurationRequired());
        }
        if (createdBy == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.createdByRequired());
        }
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            throw new StaffAccountException(StaffAccountExceptionInfo.atLeastOnePermissionRequired());
        }
    }

    private void throwIfAccountIsDisabled() {
        if (status == StaffAccountStatus.DISABLED) {
            throw new StaffAccountException(StaffAccountExceptionInfo.disabled());
        }
    }

    private void throwIfAccountIsLocked() {
        if (status == StaffAccountStatus.LOCKED) {
            if (lockedUntil != null && Instant.now().isAfter(lockedUntil)) {
                unlockAccount();
            } else {
                throw new StaffAccountException(StaffAccountExceptionInfo.locked());
            }
        }
    }

    private void unlockAccount() {
        status = isPasswordTemporary ? StaffAccountStatus.PENDING_PASSWORD_CHANGE : StaffAccountStatus.ACTIVE;
        lockedUntil = null;
        failedLoginAttempts = failedLoginAttempts.reset();
    }

    private void authenticatePassword(Password enteredPassword) {
        if (!this.password.equals(enteredPassword)) {
            failedLoginAttempts = failedLoginAttempts.increment();
            lockAccountIfMaxLoginAttemptsReached();
            throw new StaffAccountException(StaffAccountExceptionInfo.invalidCredentials());
        }
    }

    private void lockAccountIfMaxLoginAttemptsReached() {
        if (failedLoginAttempts.hasReachedMax()) {
            status = StaffAccountStatus.LOCKED;
            lockedUntil = Instant.now().plus(Duration.ofMinutes(15));
        }
    }
}
