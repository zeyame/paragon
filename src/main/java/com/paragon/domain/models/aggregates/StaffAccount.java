package com.paragon.domain.models.aggregates;

import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.models.valueobjects.*;

import java.time.Instant;
import java.time.LocalDateTime;

public class StaffAccount extends EventSourcedAggregate<DomainEvent, StaffAccountId> {

    private Username username;
    private Email email;
    private Password password;
    private Instant passwordIssuedAt;
    private boolean isTempPassword;
    private OrderAccessDuration orderAccessDuration;

    private StaffAccount(StaffAccountId id, Username username, Email email, Password password, OrderAccessDuration orderAccessDuration) {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.orderAccessDuration = orderAccessDuration;
    }

    public static StaffAccount registerWithEmail(Username username, Email email, Password password, OrderAccessDuration orderAccessDuration) {
        assertValidRegistration(username, password, orderAccessDuration);
        return new StaffAccount(StaffAccountId.generate(), username, email, password, orderAccessDuration);
    }

    public static StaffAccount registerWithoutEmail(Username username, Password password, OrderAccessDuration orderAccessDuration) {
        assertValidRegistration(username, password, orderAccessDuration);
        return new StaffAccount(StaffAccountId.generate(), username, null, password, orderAccessDuration);
    }

    private static void assertValidRegistration(Username username, Password password, OrderAccessDuration orderAccessDuration) {
        if (username == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.usernameRequired());
        }
        if (password == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.passwordRequired());
        }
        if (orderAccessDuration == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.orderAccessDurationRequired());
        }
    }
}
