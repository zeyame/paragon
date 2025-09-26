package com.paragon.domain.models.aggregates;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.models.valueobjects.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;

@Getter
public class StaffAccount extends EventSourcedAggregate<DomainEvent, StaffAccountId> {

    private Username username;
    private Email email;
    private Password password;
    private Instant passwordIssuedAt;
    private OrderAccessDuration orderAccessDuration;
    private ModmailTranscriptAccessDuration modmailTranscriptAccessDuration;
    private StaffAccountStatus status;
    private FailedLoginAttempts failedLoginAttempts;
    private Instant lockedUntil;
    private Instant lastLoginAt;
    private final StaffAccountId createdBy;
    private StaffAccountId disabledBy;
    private final Set<PermissionId> permissionIds;
    private Version version;

    private StaffAccount(StaffAccountId id,
                         Username username,
                         Email email,
                         Password password,
                         Instant passwordIssuedAt,
                         OrderAccessDuration orderAccessDuration,
                         ModmailTranscriptAccessDuration modmailTranscriptAccessDuration,
                         StaffAccountStatus status,
                         FailedLoginAttempts failedLoginAttempts,
                         Instant lockedUntil,
                         Instant lastLoginAt,
                         StaffAccountId createdBy,
                         StaffAccountId disabledBy,
                         Set<PermissionId> permissionIds,
                         Version version)
    {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.passwordIssuedAt = passwordIssuedAt;
        this.orderAccessDuration = orderAccessDuration;
        this.modmailTranscriptAccessDuration = modmailTranscriptAccessDuration;
        this.status = status;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.lastLoginAt = lastLoginAt;
        this.createdBy = createdBy;
        this.disabledBy = disabledBy;
        this.permissionIds = permissionIds;
        this.version = version;
    }

    public static StaffAccount register(Username username, Email email, Password password, OrderAccessDuration orderAccessDuration,
                                        ModmailTranscriptAccessDuration modmailTranscriptAccessDuration,
                                        StaffAccountId createdBy, Set<PermissionId> permissionIds)
    {
        assertValidRegistration(username, password, orderAccessDuration, modmailTranscriptAccessDuration, permissionIds);
        return new StaffAccount(
                StaffAccountId.generate(), username, email, password, Instant.now(),
                orderAccessDuration, modmailTranscriptAccessDuration, StaffAccountStatus.PENDING_PASSWORD_CHANGE,
                FailedLoginAttempts.initial(), null, null, createdBy, null, permissionIds, Version.initial()
        );
    }

    public static StaffAccount createFrom(StaffAccountId id, Username username, Email email, Password password,
                                          Instant passwordIssuedAt, OrderAccessDuration orderAccessDuration,
                                          ModmailTranscriptAccessDuration modmailTranscriptAccessDuration,
                                          StaffAccountStatus status, FailedLoginAttempts failedLoginAttempts,
                                          Instant lockedUntil, Instant lastLoginAt, StaffAccountId createdBy,
                                          StaffAccountId disabledBy, Set<PermissionId> permissionIds, Version version) {
        return new StaffAccount(
                id, username, email, password,
                passwordIssuedAt, orderAccessDuration,
                modmailTranscriptAccessDuration,
                status, failedLoginAttempts,
                lockedUntil, lastLoginAt,
                createdBy, disabledBy,
                permissionIds, version
        );
    }


    public boolean hasPermission(PermissionId permissionId) {
        return permissionIds.contains(permissionId);
    }

    private static void assertValidRegistration(Username username, Password password, OrderAccessDuration orderAccessDuration,
                                                ModmailTranscriptAccessDuration modmailTranscriptAccessDuration,
                                                Set<PermissionId> permissionIds)
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
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new StaffAccountException(StaffAccountExceptionInfo.atLeastOnePermissionRequired());
        }
    }
}
