package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.IpAddressException;
import com.paragon.domain.exceptions.valueobject.IpAddressExceptionInfo;
import lombok.Getter;

import java.util.List;
import java.util.regex.Pattern;

@Getter
public class IpAddress extends ValueObject {
    private final String value;

    // IPv4 pattern: matches 0.0.0.0 to 255.255.255.255
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    );

    // IPv6 pattern: comprehensive regex for all valid IPv6 formats
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +  // Full form
            "^::(?:[0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" +  // :: at start
            "^(?:[0-9a-fA-F]{1,4}:){1,6}::$|" +  // :: at end
            "^(?:[0-9a-fA-F]{1,4}:){1,6}:(?:[0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{1,4}$|" +  // :: in middle
            "^::$"  // :: only (all zeros)
    );

    private IpAddress(String value) {
        this.value = value;
    }

    public static IpAddress of(String value) {
        assertValidIpAddress(value);
        return new IpAddress(value);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static void assertValidIpAddress(String value) {
        if (value == null || value.isBlank()) {
            throw new IpAddressException(IpAddressExceptionInfo.missingValue());
        }

        boolean isValidIpv4 = IPV4_PATTERN.matcher(value).matches();
        boolean isValidIpv6 = IPV6_PATTERN.matcher(value).matches();

        if (!isValidIpv4 && !isValidIpv6) {
            throw new IpAddressException(IpAddressExceptionInfo.invalidFormat());
        }
    }
}