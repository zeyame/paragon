package com.paragon.domain.models.aggregates;

import com.paragon.domain.exceptions.aggregate.RefreshTokenException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenExceptionInfo;
import com.paragon.domain.models.valueobjects.IpAddress;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Version;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class RefreshTokenTests {
    @Nested
    class Issue {
        @Test
        void shouldIssueRefreshToken() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            IpAddress ipAddress = IpAddress.of("192.168.1.1");

            // When
            RefreshToken refreshToken = RefreshToken.issue(RefreshTokenHash.of("Hashedtoken123"), staffAccountId, ipAddress);

            // Then
            assertThat(refreshToken.getTokenHash()).isEqualTo(RefreshTokenHash.of("Hashedtoken123"));
            assertThat(refreshToken.getStaffAccountId()).isEqualTo(staffAccountId);
            assertThat(refreshToken.getIssuedFromIpAddress()).isEqualTo(ipAddress);
            assertThat(refreshToken.getExpiresAt()).isBeforeOrEqualTo(Instant.now().plus(Duration.ofDays(7)));
            assertThat(refreshToken.isRevoked()).isFalse();
            assertThat(refreshToken.getRevokedAt()).isNull();
            assertThat(refreshToken.getReplacedBy()).isNull();
            assertThat(refreshToken.getVersion()).isEqualTo(Version.initial());
        }

        @Test
        void shouldThrowRefreshTokenException_whenTokenHashIsMissing() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            IpAddress ipAddress = IpAddress.of("192.168.1.1");

            // When & Then
            assertThatExceptionOfType(RefreshTokenException.class)
                    .isThrownBy(() -> RefreshToken.issue(null, staffAccountId, ipAddress))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(
                            RefreshTokenExceptionInfo.tokenHashRequired().getMessage(),
                            RefreshTokenExceptionInfo.tokenHashRequired().getDomainErrorCode()
                    );
        }

        @Test
        void shouldThrowRefreshTokenException_whenStaffAccountIdIsMissing() {
            // Given
            RefreshTokenHash tokenHash = RefreshTokenHash.of("hashed-token");
            IpAddress ipAddress = IpAddress.of("192.168.1.1");

            // When & Then
            assertThatExceptionOfType(RefreshTokenException.class)
                    .isThrownBy(() -> RefreshToken.issue(tokenHash, null, ipAddress))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(
                            RefreshTokenExceptionInfo.staffAccountIdRequired().getMessage(),
                            RefreshTokenExceptionInfo.staffAccountIdRequired().getDomainErrorCode()
                    );
        }

        @Test
        void shouldThrowRefreshTokenException_whenIpAddressIsMissing() {
            // Given
            RefreshTokenHash tokenHash = RefreshTokenHash.of("hashed-token");
            StaffAccountId staffAccountId = StaffAccountId.generate();

            // When & Then
            assertThatExceptionOfType(RefreshTokenException.class)
                    .isThrownBy(() -> RefreshToken.issue(tokenHash, staffAccountId, null))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(
                            RefreshTokenExceptionInfo.ipAddressRequired().getMessage(),
                            RefreshTokenExceptionInfo.ipAddressRequired().getDomainErrorCode()
                    );
        }
    }

    @Nested
    class Replace {
        @Test
        void shouldRevokeCurrentToken() {
            // Given
            RefreshToken refreshToken = RefreshTokenFixture.validRefreshToken();

            // When
            refreshToken.replace();

            // Then
            assertThat(refreshToken.isRevoked()).isTrue();
        }

        @Test
        void shouldMarkOldTokenAsReplaced() {
            // Given
            RefreshToken refreshToken = RefreshTokenFixture.validRefreshToken();

            // When
            refreshToken.replace();

            // Then
            assertThat(refreshToken.getReplacedBy()).isNotNull();
        }

        @Test
        void shouldThrowIfRefreshTokenIsRevoked() {
            // Given
            RefreshToken refreshToken = RefreshTokenFixture.revokedRefreshToken();

            // When & Then
            assertThatExceptionOfType(RefreshTokenException.class)
                    .isThrownBy(refreshToken::replace)
                    .extracting("domainErrorCode", "message")
                    .containsExactly(
                            RefreshTokenExceptionInfo.tokenAlreadyRevoked().getDomainErrorCode(),
                            RefreshTokenExceptionInfo.tokenAlreadyRevoked().getMessage()
                    );
        }
    }
}
