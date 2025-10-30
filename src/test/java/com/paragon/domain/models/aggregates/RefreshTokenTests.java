package com.paragon.domain.models.aggregates;

import com.paragon.domain.exceptions.aggregate.RefreshTokenException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenExceptionInfo;
import com.paragon.domain.interfaces.TokenHasher;
import com.paragon.domain.models.valueobjects.IpAddress;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Version;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RefreshTokenTests {
    @Nested
    class Issue {
        private final TokenHasher tokenHasherMock;

        Issue() {
            tokenHasherMock = mock(TokenHasher.class);
            when(tokenHasherMock.hash(anyString())).thenReturn("Hashedtoken123");
        }

        @Test
        void shouldIssueRefreshToken() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            IpAddress ipAddress = IpAddress.of("192.168.1.1");

            // When
            RefreshToken refreshToken = RefreshToken.issue(staffAccountId, ipAddress, tokenHasherMock);

            // Then
            assertThat(refreshToken.getTokenHash()).isEqualTo(RefreshTokenHash.fromHashed("Hashedtoken123"));
            assertThat(refreshToken.getStaffAccountId()).isEqualTo(staffAccountId);
            assertThat(refreshToken.getIssuedFromIpAddress()).isEqualTo(ipAddress);
            assertThat(refreshToken.getExpiresAt()).isBeforeOrEqualTo(Instant.now().plus(Duration.ofDays(7)));
            assertThat(refreshToken.isRevoked()).isFalse();
            assertThat(refreshToken.getRevokedAt()).isNull();
            assertThat(refreshToken.getReplacedBy()).isNull();
            assertThat(refreshToken.getVersion()).isEqualTo(Version.initial());
        }

        @Test
        void shouldThrowRefreshTokenException_whenStaffAccountIdIsMissing() {
            // Given
            IpAddress ipAddress = IpAddress.of("192.168.1.1");

            // When & Then
            assertThatExceptionOfType(RefreshTokenException.class)
                    .isThrownBy(() -> RefreshToken.issue(null, ipAddress, tokenHasherMock))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(
                            RefreshTokenExceptionInfo.staffAccountIdRequired().getMessage(),
                            RefreshTokenExceptionInfo.staffAccountIdRequired().getDomainErrorCode()
                    );
        }

        @Test
        void shouldThrowRefreshTokenException_whenIpAddressIsMissing() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();

            // When & Then
            assertThatExceptionOfType(RefreshTokenException.class)
                    .isThrownBy(() -> RefreshToken.issue(staffAccountId, null, tokenHasherMock))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(
                            RefreshTokenExceptionInfo.ipAddressRequired().getMessage(),
                            RefreshTokenExceptionInfo.ipAddressRequired().getDomainErrorCode()
                    );
        }
    }
}
