package com.paragon.application.events.refreshtokens;

import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.interfaces.repos.RefreshTokenWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

public class StaffAccountRefreshTokenRevocationHandlerTests {
    private final StaffAccountRefreshTokenRevocationHandler sut;
    private final RefreshTokenWriteRepo refreshTokenWriteRepoMock;

    StaffAccountRefreshTokenRevocationHandlerTests() {
        this.refreshTokenWriteRepoMock = mock(RefreshTokenWriteRepo.class);
        sut = new StaffAccountRefreshTokenRevocationHandler(refreshTokenWriteRepoMock);
    }

    @ParameterizedTest
    @MethodSource("provideEvents")
    void shouldRevokeAllTokensForStaffAccount(StaffAccountLockedEvent event) {
        // Given
        ArgumentCaptor<List<RefreshToken>> revokedTokensCaptor = ArgumentCaptor.forClass(List.class);
        List<RefreshToken> activeTokens = List.of(RefreshTokenFixture.validRefreshToken(), RefreshTokenFixture.validRefreshToken());

        when(refreshTokenWriteRepoMock.getActiveTokensByStaffAccountId(any(StaffAccountId.class)))
                .thenReturn(activeTokens);

        // When
        sut.handle(event);

        // Then
        for (RefreshToken token : activeTokens) {
            assertThat(token.isRevoked()).isTrue();
        }

        verify(refreshTokenWriteRepoMock, times(1)).updateAll(revokedTokensCaptor.capture());
        List<RefreshToken> revokedTokens = revokedTokensCaptor.getValue();
        assertThat(revokedTokens.size()).isEqualTo(activeTokens.size());
    }

    @Test
    void shouldCatchDomainException() {
        // Given
        List<RefreshToken> refreshTokens = List.of(
                new RefreshTokenFixture()
                        .withRevoked(true) // forces a domain exception
                        .withRevokedAt(Instant.now())
                        .build()
        );

        when(refreshTokenWriteRepoMock.getActiveTokensByStaffAccountId(any(StaffAccountId.class)))
                .thenReturn(refreshTokens);

        // When & Then
        assertThatNoException()
                .isThrownBy(() -> sut.handle(new StaffAccountLockedEvent(StaffAccountFixture.validStaffAccount())));
    }

    @Test
    void shouldCatchInfraException() {
        // Given
        doThrow(InfraException.class)
                .when(refreshTokenWriteRepoMock)
                .getActiveTokensByStaffAccountId(any(StaffAccountId.class));

        // When & Then
        assertThatNoException()
                .isThrownBy(() -> sut.handle(new StaffAccountLockedEvent(StaffAccountFixture.validStaffAccount())));
    }

    private static Stream<Arguments> provideEvents() {
        return Stream.of(
                Arguments.of(new StaffAccountLockedEvent(StaffAccountFixture.validStaffAccount()))
        );
    }
}
