package com.paragon.application.commands;

import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommand;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommandHandler;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommandResponse;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.application.common.interfaces.TokenHasher;
import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.application.common.interfaces.StaffAccountRefreshTokenRevocationService;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repositories.RefreshTokenWriteRepo;
import com.paragon.domain.interfaces.repositories.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.PlaintextRefreshToken;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.exceptions.InfraExceptionHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RefreshStaffAccountTokenCommandHandlerTests {
    private final RefreshStaffAccountTokenCommandHandler sut;
    private final RefreshTokenWriteRepo refreshTokenWriteRepoMock;
    private final StaffAccountWriteRepo staffAccountWriteRepoMock;
    private final StaffAccountRefreshTokenRevocationService staffAccountRefreshTokenRevocationServiceMock;
    private final UnitOfWork unitOfWorkMock;
    private final TokenHasher tokenHasherMock;
    private final AppExceptionHandler appExceptionHandlerMock;
    private final InfraExceptionHandler infraExceptionHandlerMock;

    public RefreshStaffAccountTokenCommandHandlerTests() {
        refreshTokenWriteRepoMock = mock(RefreshTokenWriteRepo.class);
        staffAccountWriteRepoMock = mock(StaffAccountWriteRepo.class);
        staffAccountRefreshTokenRevocationServiceMock = mock(StaffAccountRefreshTokenRevocationService.class);
        unitOfWorkMock = mock(UnitOfWork.class);
        tokenHasherMock = mock(TokenHasher.class);
        appExceptionHandlerMock = mock(AppExceptionHandler.class);
        infraExceptionHandlerMock = mock(InfraExceptionHandler.class);
        sut = new RefreshStaffAccountTokenCommandHandler(
                refreshTokenWriteRepoMock, staffAccountWriteRepoMock, staffAccountRefreshTokenRevocationServiceMock,
                unitOfWorkMock, tokenHasherMock, appExceptionHandlerMock, infraExceptionHandlerMock
        );
    }

    @Test
    void shouldBeginTransaction() {
        // Given
        setupHappyScenario(RefreshTokenFixture.validRefreshToken(), StaffAccountFixture.validStaffAccount());

        // When
        sut.handle(new RefreshStaffAccountTokenCommand("plain-token"));

        // Then
        verify(unitOfWorkMock, times(1)).begin();
    }

    @Test
    void shouldCommitTransaction() {
        // Given
        setupHappyScenario(RefreshTokenFixture.validRefreshToken(), StaffAccountFixture.validStaffAccount());

        // When
        sut.handle(new RefreshStaffAccountTokenCommand("plain-token"));

        // Then
        verify(unitOfWorkMock, times(1)).commit();
    }

    @Test
    void shouldReplaceOldTokenWithNewOne() {
        // Given
        RefreshStaffAccountTokenCommand command = new RefreshStaffAccountTokenCommand("plain-token");
        RefreshToken oldToken = RefreshTokenFixture.validRefreshToken();

        setupHappyScenario(oldToken, StaffAccountFixture.validStaffAccount());

        // When
        sut.handle(command);

        // Then
        ArgumentCaptor<RefreshToken> newTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenWriteRepoMock, times(1))
                .update(oldToken);
        verify(refreshTokenWriteRepoMock, times(1))
                .create(newTokenCaptor.capture());
        RefreshToken newToken = newTokenCaptor.getValue();

        assertThat(oldToken.isRevoked()).isTrue();
        assertThat(oldToken.getReplacedBy()).isEqualTo(newToken.getId());
        assertThat(newToken.getStaffAccountId()).isEqualTo(oldToken.getStaffAccountId());
        assertThat(newToken.getIssuedFromIpAddress()).isEqualTo(oldToken.getIssuedFromIpAddress());
    }

    @Test
    void shouldReturnExpectedCommandResponse() {
        // Given
        RefreshStaffAccountTokenCommand command = new RefreshStaffAccountTokenCommand("plain-token");

        StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();
        RefreshToken oldToken = new RefreshTokenFixture()
                .withStaffAccountId(staffAccount.getId().getValue().toString())
                .build();
        setupHappyScenario(oldToken, staffAccount);

        // When
        RefreshStaffAccountTokenCommandResponse commandResponse = sut.handle(command);

        // Then
        assertThat(commandResponse.staffAccountId()).isEqualTo(staffAccount.getId().getValue().toString());
        assertThat(commandResponse.username()).isEqualTo(staffAccount.getUsername().getValue());
        assertThat(commandResponse.requiresPasswordReset()).isEqualTo(staffAccount.requiresPasswordReset());
        assertThat(commandResponse.plainRefreshToken()).isNotNull();
        assertThat(commandResponse.permissionCodes())
                .isEqualTo(staffAccount.getPermissionCodes()
                        .stream().map(PermissionCode::getValue)
                        .toList()
                );
        assertThat(commandResponse.version()).isEqualTo(staffAccount.getVersion().getValue());
    }

    @Test
    void shouldRevokeAllOtherStaffAccountTokens_whenTheProvidedTokenIsAlreadyRevoked() {
        // Given
        RefreshStaffAccountTokenCommand command = new RefreshStaffAccountTokenCommand("plain-token");

        RefreshToken revokedToken = RefreshTokenFixture.revokedRefreshToken();
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(revokedToken.getTokenHash());
        when(refreshTokenWriteRepoMock.getByTokenHash(revokedToken.getTokenHash()))
                .thenReturn(Optional.of(revokedToken));

        // When & Then
        assertThatException().isThrownBy(() -> sut.handle(command));
        verify(staffAccountRefreshTokenRevocationServiceMock, times(1))
                .revokeAllTokensForStaffAccount(revokedToken.getStaffAccountId());
    }

    @Test
    void shouldCommitTransactionAfterCallingRevocationService() {
        // Given
        RefreshStaffAccountTokenCommand command = new RefreshStaffAccountTokenCommand("plain-token");

        RefreshToken revokedToken = RefreshTokenFixture.revokedRefreshToken();
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(revokedToken.getTokenHash());
        when(refreshTokenWriteRepoMock.getByTokenHash(revokedToken.getTokenHash()))
                .thenReturn(Optional.of(revokedToken));

        // When & Then
        assertThatException().isThrownBy(() -> sut.handle(command));
        verify(unitOfWorkMock, times(1)).commit();
    }

    @Test
    void shouldRollbackTransaction_whenStaffAccountDoesNotExist() {
        // Given
        RefreshStaffAccountTokenCommand command = new RefreshStaffAccountTokenCommand("plain-token");

        RefreshToken oldToken = RefreshTokenFixture.validRefreshToken();
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(oldToken.getTokenHash());
        when(refreshTokenWriteRepoMock.getByTokenHash(oldToken.getTokenHash()))
                .thenReturn(Optional.of(oldToken));
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatException().isThrownBy(() -> sut.handle(command));
        verify(unitOfWorkMock, times(1)).rollback();
    }

    @Test
    void shouldRollbackTransaction_whenDomainExceptionIsThrown() {
        // Given
        RefreshStaffAccountTokenCommand command = new RefreshStaffAccountTokenCommand("");

        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatException().isThrownBy(() -> sut.handle(command));
        verify(unitOfWorkMock, times(1)).rollback();
    }

    @Test
    void shouldRollbackTransaction_whenInfraExceptionIsThrown() {
        // Given
        RefreshStaffAccountTokenCommand command = new RefreshStaffAccountTokenCommand("plain-token");

        setupHappyScenario(RefreshTokenFixture.validRefreshToken(), StaffAccountFixture.validStaffAccount());

        doThrow(InfraException.class)
                .when(refreshTokenWriteRepoMock)
                .update(any(RefreshToken.class));
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatException().isThrownBy(() -> sut.handle(command));
        verify(unitOfWorkMock, times(1)).rollback();
    }

    @Test
    void shouldThrowAppException_whenTokenHashDoesNotExist() {
        // Given
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(RefreshTokenHash.of("token-hash"));
        when(refreshTokenWriteRepoMock.getByTokenHash(any(RefreshTokenHash.class)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(new RefreshStaffAccountTokenCommand("plain-token")))
                .extracting("message", "errorCode")
                .containsExactly(AppExceptionInfo.invalidRefreshToken().getMessage(), AppExceptionInfo.invalidRefreshToken().getAppErrorCode());
    }

    @Test
    void shouldThrowAppException_whenTokenIsExpired() {
        // Given
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(RefreshTokenHash.of("token-hash"));
        when(refreshTokenWriteRepoMock.getByTokenHash(any(RefreshTokenHash.class)))
                .thenReturn(Optional.of(RefreshTokenFixture.expiredRefreshToken()));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(new RefreshStaffAccountTokenCommand("plain-token")))
                .extracting("message", "errorCode")
                .containsExactly(AppExceptionInfo.invalidRefreshToken().getMessage(), AppExceptionInfo.invalidRefreshToken().getAppErrorCode());
    }

    @Test
    void shouldThrowAppException_whenTokenIsRevoked() {
        // Given
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(RefreshTokenHash.of("token-hash"));
        when(refreshTokenWriteRepoMock.getByTokenHash(any(RefreshTokenHash.class)))
                .thenReturn(Optional.of(RefreshTokenFixture.revokedRefreshToken()));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(new RefreshStaffAccountTokenCommand("plain-token")))
                .extracting("message", "errorCode")
                .containsExactly(AppExceptionInfo.invalidRefreshToken().getMessage(), AppExceptionInfo.invalidRefreshToken().getAppErrorCode());
    }

    @Test
    void shouldThrowAppException_whenStaffAccountDoesNotExist() {
        // Given
        RefreshToken refreshToken = RefreshTokenFixture.validRefreshToken();
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(RefreshTokenHash.of("token-hash"));
        when(refreshTokenWriteRepoMock.getByTokenHash(any(RefreshTokenHash.class)))
                .thenReturn(Optional.of(refreshToken));
        when(staffAccountWriteRepoMock.getById(any(StaffAccountId.class)))
                .thenReturn(Optional.empty());

        String staffAccountId = refreshToken.getStaffAccountId().getValue().toString();

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(new RefreshStaffAccountTokenCommand("plain-token")))
                .extracting("message", "errorCode")
                .containsExactly(
                        AppExceptionInfo.staffAccountNotFound(staffAccountId).getMessage(),
                        AppExceptionInfo.staffAccountNotFound(staffAccountId).getAppErrorCode()
                );
    }

    @Test
    void shouldCatchDomainException_andTranslateToAppException() {
        // Given
        RefreshStaffAccountTokenCommand command = new RefreshStaffAccountTokenCommand("");  // forces a domain exception

        when(appExceptionHandlerMock.handleDomainException(any(DomainException.class)))
                .thenThrow(mock(AppException.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command));
    }

    @Test
    void shouldCatchInfraException_andTranslateToAppException() {
        // Given
        RefreshStaffAccountTokenCommand command = new RefreshStaffAccountTokenCommand("plain-token");

        setupHappyScenario(RefreshTokenFixture.validRefreshToken(), StaffAccountFixture.validStaffAccount());

        doThrow(InfraException.class)
                .when(refreshTokenWriteRepoMock)
                .update(any(RefreshToken.class));
        when(appExceptionHandlerMock.handleInfraException(any(InfraException.class)))
                .thenReturn(mock(AppException.class));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.handle(command));
    }

    private void setupHappyScenario(RefreshToken refreshToken, StaffAccount staffAccount) {
        when(tokenHasherMock.hash(any(PlaintextRefreshToken.class)))
                .thenReturn(refreshToken.getTokenHash());
        when(refreshTokenWriteRepoMock.getByTokenHash(refreshToken.getTokenHash()))
                .thenReturn(Optional.of(refreshToken));
        when(staffAccountWriteRepoMock.getById(refreshToken.getStaffAccountId()))
                .thenReturn(Optional.of(staffAccount));
    }
}
