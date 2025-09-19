package com.paragon.domain.models.aggregates;

import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.models.valueobjects.Email;
import com.paragon.domain.models.valueobjects.Password;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;

public class StaffAccount extends EventSourcedAggregate<DomainEvent, StaffAccountId> {

    private Username username;
    private Email email;
    private Password password;

    private StaffAccount(StaffAccountId id, Username username, Email email, Password password) {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public static StaffAccount registerWithEmail(Username username, Email email, Password password) {
        if (username == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.usernameRequired());
        }
        if (password == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.passwordRequired());
        }
        return new StaffAccount(StaffAccountId.generate(), username, email, password);
    }

    public static StaffAccount registerWithoutEmail(Username username, Password password) {
        if (username == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.usernameRequired());
        }
        if (password == null) {
            throw new StaffAccountException(StaffAccountExceptionInfo.passwordRequired());
        }
        return new StaffAccount(StaffAccountId.generate(), username, null, password);
    }
}
