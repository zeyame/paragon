package com.paragon.domain.events.staffaccountevents;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;

@Getter
public abstract class StaffAccountEventBase extends DomainEvent {

    private final StaffAccountId staffAccountId;
    private final Username username;
    private final Email email;
    private final Password password;
    private final Instant passwordIssuedAt;
    private final OrderAccessDuration orderAccessDuration;
    private final ModmailTranscriptAccessDuration modmailTranscriptAccessDuration;
    private final StaffAccountStatus staffAccountStatus;
    private final FailedLoginAttempts failedLoginAttempts;
    private final Instant lockedUntil;
    private final Instant lastLoginAt;
    private final StaffAccountId staffAccountCreatedBy;
    private final StaffAccountId staffAccountDisabledBy;
    private final Set<PermissionCode> permissionCodes;
    private final Version staffAccountVersion;

    protected StaffAccountEventBase(StaffAccount staffAccount, String eventName) {
        super(EventId.generate(), eventName);

        this.staffAccountId = staffAccount.getId();
        this.username = staffAccount.getUsername();
        this.email = staffAccount.getEmail();
        this.password = staffAccount.getPassword();
        this.passwordIssuedAt = staffAccount.getPasswordIssuedAt();
        this.orderAccessDuration = staffAccount.getOrderAccessDuration();
        this.modmailTranscriptAccessDuration = staffAccount.getModmailTranscriptAccessDuration();
        this.staffAccountStatus = staffAccount.getStatus();
        this.failedLoginAttempts = staffAccount.getFailedLoginAttempts();
        this.lockedUntil = staffAccount.getLockedUntil();
        this.lastLoginAt = staffAccount.getLastLoginAt();
        this.staffAccountCreatedBy = staffAccount.getCreatedBy();
        this.staffAccountDisabledBy = staffAccount.getDisabledBy();
        this.permissionCodes = staffAccount.getPermissionCodes();
        this.staffAccountVersion = staffAccount.getVersion();
    }
}
