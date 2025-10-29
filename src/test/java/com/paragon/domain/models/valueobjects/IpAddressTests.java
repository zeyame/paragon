package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.IpAddressException;
import com.paragon.domain.exceptions.valueobject.IpAddressExceptionInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class IpAddressTests {

    @Nested
    class Of {
        @ParameterizedTest
        @ValueSource(strings = {
                "192.168.1.1",
                "10.0.0.1",
                "172.16.0.1",
                "255.255.255.255",
                "0.0.0.0",
                "2001:0db8:85a3:0000:0000:8a2e:0370:7334",
                "2001:db8:85a3::8a2e:370:7334",
                "::1",
                "fe80::1",
                "::"
        })
        void shouldCreateValidIpAddresses(String validIp) {
            // When
            IpAddress ipAddress = IpAddress.of(validIp);

            // Then
            assertThat(ipAddress.getValue()).isEqualTo(validIp);
        }

        @ParameterizedTest
        @MethodSource("invalidIpAddresses")
        void shouldRejectInvalidIpAddresses(String invalidIp, IpAddressExceptionInfo exceptionInfo) {
            // Given
            String expectedErrorMessage = exceptionInfo.getMessage();
            int expectedErrorCode = exceptionInfo.getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(IpAddressException.class)
                    .isThrownBy(() -> IpAddress.of(invalidIp))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        private static Stream<Arguments> invalidIpAddresses() {
            return Stream.of(
                    Arguments.of(null, IpAddressExceptionInfo.missingValue()),
                    Arguments.of("", IpAddressExceptionInfo.missingValue()),
                    Arguments.of("   ", IpAddressExceptionInfo.missingValue()),
                    Arguments.of("256.1.1.1", IpAddressExceptionInfo.invalidFormat()),
                    Arguments.of("192.168.1", IpAddressExceptionInfo.invalidFormat()),
                    Arguments.of("192.168.1.1.1", IpAddressExceptionInfo.invalidFormat()),
                    Arguments.of("192.168.-1.1", IpAddressExceptionInfo.invalidFormat()),
                    Arguments.of("abc.def.ghi.jkl", IpAddressExceptionInfo.invalidFormat()),
                    Arguments.of("gggg::1", IpAddressExceptionInfo.invalidFormat()),
                    Arguments.of("just some text", IpAddressExceptionInfo.invalidFormat())
            );
        }
    }
}