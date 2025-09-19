package com.paragon.domain.models.aggregates;

import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.exceptions.valueobject.StaffAccountIdExceptionInfo;
import com.paragon.domain.models.valueobjects.Email;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;

import java.util.Optional;

public class StaffAccount extends EventSourcedAggregate<DomainEvent, StaffAccountId> {

    private Username username;
    private Email email;

    private StaffAccount(StaffAccountId id, Username username, Email email) {
        super(id);
        this.username = username;
        this.email = email;
    }

    public static StaffAccount registerWithEmail(Username username, Email email) {
        if (username == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.missingUsername());
        }
        return new StaffAccount(StaffAccountId.generate(), username, email);
    }

    public static StaffAccount registerWithoutEmail(Username username) {
        if (username == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.missingUsername());
        }
        return new StaffAccount(StaffAccountId.generate(), username, null);
    }
}
