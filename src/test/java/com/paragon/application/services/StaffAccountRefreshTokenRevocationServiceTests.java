package com.paragon.application.services;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionStatusCode;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenExceptionInfo;
import com.paragon.domain.interfaces.RefreshTokenWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StaffAccountRefreshTokenRevocationServiceTests {
    private final StaffAccountRefreshTokenRevocationService sut;
    private final RefreshTokenWriteRepo refreshTokenWriteRepoMock;
    private final AppExceptionHandler appExceptionHandlerMock;

    public StaffAccountRefreshTokenRevocationServiceTests() {
        refreshTokenWriteRepoMock = mock(RefreshTokenWriteRepo.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        sut = new StaffAccountRefreshTokenRevocationServiceImpl(refreshTokenWriteRepoMock, appExceptionHandlerMock);
    }

    @Test
    void shouldRevokeAllTokensForStaffAccount() {
        // Given
        ArgumentCaptor<List<RefreshToken>> revokedTokensCaptor = ArgumentCaptor.forClass(List.class);
        List<RefreshToken> activeTokens = List.of(RefreshTokenFixture.validRefreshToken(), RefreshTokenFixture.validRefreshToken());

        when(refreshTokenWriteRepoMock.getActiveTokensByStaffAccountId(any(StaffAccountId.class)))
                .thenReturn(activeTokens);

        // When
        sut.revokeAllTokensForStaffAccount(StaffAccountId.generate());

        // Then
        for (RefreshToken token : activeTokens) {
            assertThat(token.isRevoked()).isTrue();
        }

        verify(refreshTokenWriteRepoMock, times(1)).updateAll(revokedTokensCaptor.capture());
        List<RefreshToken> revokedTokens = revokedTokensCaptor.getValue();
        assertThat(revokedTokens.size()).isEqualTo(activeTokens.size());
    }

    @Test
    void shouldCatchDomainException_andTranslateToAppException() {
        // Given
        List<RefreshToken> tokens = List.of(RefreshTokenFixture.validRefreshToken(), RefreshTokenFixture.revokedRefreshToken());

        when(refreshTokenWriteRepoMock.getActiveTokensByStaffAccountId(any(StaffAccountId.class)))
                .thenReturn(tokens);

        RefreshTokenException refreshTokenException = new RefreshTokenException(RefreshTokenExceptionInfo.tokenAlreadyRevoked());
        AppException expectedAppException = new AppException(refreshTokenException, AppExceptionStatusCode.INVALID_RESOURCE_STATE);
        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(expectedAppException);

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.revokeAllTokensForStaffAccount(StaffAccountId.generate()))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(expectedAppException.getMessage(), expectedAppException.getErrorCode(), expectedAppException.getStatusCode());
    }

    @Test
    void shouldCatchInfraException_andTranslateToAppException() {
        // Given
        doThrow(InfraException.class)
                .when(refreshTokenWriteRepoMock)
                .getActiveTokensByStaffAccountId(any(StaffAccountId.class));

        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.revokeAllTokensForStaffAccount(StaffAccountId.generate()));
    }
}
