package com.paragon.application.commands;

import com.paragon.application.commands.logoutstaffaccount.LogoutStaffAccountCommand;
import com.paragon.application.commands.logoutstaffaccount.LogoutStaffAccountCommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.TokenHasher;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.RefreshTokenWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.PlaintextRefreshToken;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LogoutStaffAccountCommandHandlerTests {

    private LogoutStaffAccountCommandHandler sut;
    private RefreshTokenWriteRepo refreshTokenWriteRepoMock;
    private TokenHasher tokenHasherMock;
    private AppExceptionHandler appExceptionHandlerMock;

    @BeforeEach
    void setup() {
        refreshTokenWriteRepoMock = mock(RefreshTokenWriteRepo.class);
        tokenHasherMock = mock(TokenHasher.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);

        sut = new LogoutStaffAccountCommandHandler(
                refreshTokenWriteRepoMock,
                tokenHasherMock,
                appExceptionHandlerMock
        );
    }

    @Test
    void shouldRevokeRefreshTokenAndPersist() {
        // Given
        RefreshToken refreshToken = setupValidRefreshTokenScenario();

        // When
        sut.handle(new LogoutStaffAccountCommand("plain-token"));

        // Then
        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenWriteRepoMock).update(refreshToken);
    }

    @Test
    void shouldHashProvidedToken() {
        // Given
        RefreshToken refreshToken = setupValidRefreshTokenScenario();

        // When
        sut.handle(new LogoutStaffAccountCommand("plain-token"));

        // Then
        ArgumentCaptor<PlaintextRefreshToken> plainTokenCaptor = ArgumentCaptor.forClass(PlaintextRefreshToken.class);
        verify(tokenHasherMock).hash(plainTokenCaptor.capture());
        assertThat(plainTokenCaptor.getValue().getValue()).isEqualTo("plain-token");
        verify(refreshTokenWriteRepoMock).update(refreshToken);
    }

    @Test
    void shouldThrowAppException_whenRefreshTokenDoesNotExist() {
        // Given
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(RefreshTokenHash.of("hashed-token"));
        when(refreshTokenWriteRepoMock.getByTokenHash(RefreshTokenHash.of("hashed-token")))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(new LogoutStaffAccountCommand("plain-token")))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(
                        AppExceptionInfo.invalidRefreshToken().getMessage(),
                        AppExceptionInfo.invalidRefreshToken().getAppErrorCode(),
                        AppExceptionInfo.invalidRefreshToken().getStatusCode()
                );

        verify(refreshTokenWriteRepoMock, never()).update(any(RefreshToken.class));
    }

    @Test
    void shouldTranslateDomainExceptionToAppException() {
        // Given
        RefreshToken revokedToken = RefreshTokenFixture.revokedRefreshToken();
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(revokedToken.getTokenHash());
        when(refreshTokenWriteRepoMock.getByTokenHash(revokedToken.getTokenHash()))
                .thenReturn(Optional.of(revokedToken));
        AppException expectedException = new AppException(AppExceptionInfo.invalidRefreshToken());
        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(expectedException);

        // When & Then
        assertThatThrownBy(() -> sut.handle(new LogoutStaffAccountCommand("plain-token")))
                .isEqualTo(expectedException);

        verify(appExceptionHandlerMock).handleDomainException(any(DomainException.class));
    }

    @Test
    void shouldTranslateInfraExceptionToAppException() {
        // Given
        RefreshToken refreshToken = setupValidRefreshTokenScenario();

        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));
        doThrow(InfraException.class)
                .when(refreshTokenWriteRepoMock).update(refreshToken);

        // When & Then
        assertThatThrownBy(() -> sut.handle(new LogoutStaffAccountCommand("plain-token")))
                .isInstanceOf(AppException.class);
    }

    private RefreshToken setupValidRefreshTokenScenario() {
        RefreshToken refreshToken = RefreshTokenFixture.validRefreshToken();
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(refreshToken.getTokenHash());
        when(refreshTokenWriteRepoMock.getByTokenHash(refreshToken.getTokenHash()))
                .thenReturn(Optional.of(refreshToken));
        return refreshToken;
    }
}
