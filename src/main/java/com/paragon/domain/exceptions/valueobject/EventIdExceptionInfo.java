package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class EventIdExceptionInfo extends DomainExceptionInfo {
    private EventIdExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static EventIdExceptionInfo mustNotBeNull() {
        return new EventIdExceptionInfo("Event ID cannot be null and must be of valid format.", 101001);
    }
}
