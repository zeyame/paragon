package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class OrderAccessDurationExceptionInfo extends DomainExceptionInfo {
    private OrderAccessDurationExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static OrderAccessDurationExceptionInfo mustBePositive() {
        return new OrderAccessDurationExceptionInfo(
                "Order access duration must be greater than zero",
                106001
        );
    }
}
