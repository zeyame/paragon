package com.paragon.domain.models.aggregates;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.models.valueobjects.*;
import lombok.Getter;

import java.time.Instant;

@Getter
public class StaffAccount extends EventSourcedAggregate<DomainEvent, StaffAccountId> {

    private Username username;
    private Email email;
    private Password password;
    private Instant passwordIssuedAt;
    private boolean isTempPassword;
    private OrderAccessDuration orderAccessDuration;
    private ModmailTranscriptAccessDuration modmailTranscriptAccessDuration;
    private StaffAccountStatus status;
    private FailedLoginAttempts failedLoginAttempts;
    private Instant lockedUntil;
    private Instant lastLoginAt;
    private final StaffAccountId registeredBy;
    private StaffAccountId disabledBy;

    private StaffAccount(StaffAccountId id, Username username, Email email, Password password, Instant passwordIssuedAt, boolean isTempPassword, OrderAccessDuration orderAccessDuration, ModmailTranscriptAccessDuration modmailTranscriptAccessDuration, StaffAccountStatus status, FailedLoginAttempts failedLoginAttempts, Instant lockedUntil, Instant lastLoginAt, StaffAccountId registeredBy, StaffAccountId disabledBy) {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.passwordIssuedAt = passwordIssuedAt;
        this.isTempPassword = isTempPassword;
        this.orderAccessDuration = orderAccessDuration;
        this.modmailTranscriptAccessDuration = modmailTranscriptAccessDuration;
        this.status = status;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.lastLoginAt = lastLoginAt;
        this.registeredBy = registeredBy;
        this.disabledBy = disabledBy;
    }

    public static StaffAccount register(Username username, Email email, Password password, OrderAccessDuration orderAccessDuration, ModmailTranscriptAccessDuration modmailTranscriptAccessDuration, StaffAccountId registeredBy) {
        assertValidRegistration(username, password, orderAccessDuration, modmailTranscriptAccessDuration);
        return new StaffAccount(StaffAccountId.generate(), username, email, password, Instant.now(), true, orderAccessDuration, modmailTranscriptAccessDuration, StaffAccountStatus.REGISTERED, FailedLoginAttempts.initial(), null, null, registeredBy, null);
    }

    private static void assertValidRegistration(Username username, Password password, OrderAccessDuration orderAccessDuration, ModmailTranscriptAccessDuration modmailTranscriptAccessDuration) {
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
    }
}
