package com.paragon.domain.services;

import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenExceptionInfo;
import com.paragon.domain.exceptions.services.RefreshTokenRevocationServiceException;
import com.paragon.domain.exceptions.services.RefreshTokenRevocationServiceExceptionInfo;
import com.paragon.domain.interfaces.repos.RefreshTokenWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RefreshTokenRevocationServiceTests {
    @Nested
    class RevokeAllTokensForStaffAccount {
        private final RefreshTokenRevocationServiceImpl sut;
        private final RefreshTokenWriteRepo refreshTokenWriteRepoMock;

        RevokeAllTokensForStaffAccount() {
            refreshTokenWriteRepoMock = mock(RefreshTokenWriteRepo.class);
            sut = new RefreshTokenRevocationServiceImpl(refreshTokenWriteRepoMock);
        }

        @Test
        void shouldRevokeAllTokensForStaffAccount() {
            // Given
            List<RefreshToken> refreshTokens = List.of(RefreshTokenFixture.validRefreshToken(), RefreshTokenFixture.validRefreshToken());

            when(refreshTokenWriteRepoMock.getActiveTokensByStaffAccountId(any(StaffAccountId.class)))
                    .thenReturn(refreshTokens);

            // When
            sut.revokeAllTokensForStaffAccount(StaffAccountId.generate());

            // Then
            for (RefreshToken token : refreshTokens) {
                assertThat(token.isRevoked()).isTrue();
            }
        }

        @Test
        void shouldThrowRefreshTokenRevocationServiceException_whenNoActiveRefreshTokensFound() {
            // Given
            when(refreshTokenWriteRepoMock.getActiveTokensByStaffAccountId(any(StaffAccountId.class)))
                    .thenReturn(List.of());

            String expectedErrorMessage = RefreshTokenRevocationServiceExceptionInfo.noActiveTokensFound().getMessage();
            int domainErrorCode = RefreshTokenRevocationServiceExceptionInfo.noActiveTokensFound().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(RefreshTokenRevocationServiceException.class)
                    .isThrownBy(() -> sut.revokeAllTokensForStaffAccount(StaffAccountId.generate()))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, domainErrorCode);
        }

        @Test
        void shouldPropagateDomainException() {
            // Given
            RefreshToken refreshToken = RefreshTokenFixture.validRefreshToken();
            refreshToken.revoke();

            when(refreshTokenWriteRepoMock.getActiveTokensByStaffAccountId(any(StaffAccountId.class)))
                    .thenReturn(List.of(refreshToken));

            String expectedErrorMessage = RefreshTokenExceptionInfo.tokenAlreadyRevoked().getMessage();
            int domainErrorCode = RefreshTokenExceptionInfo.tokenAlreadyRevoked().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(RefreshTokenException.class)
                    .isThrownBy(() -> sut.revokeAllTokensForStaffAccount(StaffAccountId.generate()))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, domainErrorCode);
        }

        @Test
        void shouldPropagateInfraException() {
            // Given
            doThrow(InfraException.class)
                    .when(refreshTokenWriteRepoMock)
                    .getActiveTokensByStaffAccountId(any(StaffAccountId.class));

            // When & Then
            assertThatExceptionOfType(InfraException.class)
                    .isThrownBy(() -> sut.revokeAllTokensForStaffAccount(StaffAccountId.generate()));
        }
    }
}
