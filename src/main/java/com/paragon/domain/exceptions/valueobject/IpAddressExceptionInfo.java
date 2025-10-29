package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class IpAddressExceptionInfo extends DomainExceptionInfo {
    private IpAddressExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }

    public static IpAddressExceptionInfo missingValue() {
        return new IpAddressExceptionInfo(
                "IP address must not be null or empty.",
                110001
        );
    }

    public static IpAddressExceptionInfo invalidFormat() {
        return new IpAddressExceptionInfo(
                "IP address has an invalid format (must be a valid IPv4 or IPv6 address).",
                110002
        );
    }
}