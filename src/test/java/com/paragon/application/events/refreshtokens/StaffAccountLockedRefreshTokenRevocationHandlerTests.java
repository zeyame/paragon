package com.paragon.application.events.refreshtokens;

import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.services.RefreshTokenRevocationService;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

public class StaffAccountLockedRefreshTokenRevocationHandlerTests {
    private final StaffAccountLockedRefreshTokenRevocationHandler sut;
    private final RefreshTokenRevocationService refreshTokenRevocationServiceMock;
    private final StaffAccountLockedEvent staffAccountLockedEvent;

    StaffAccountLockedRefreshTokenRevocationHandlerTests() {
        this.refreshTokenRevocationServiceMock = mock(RefreshTokenRevocationService.class);
        sut = new StaffAccountLockedRefreshTokenRevocationHandler(refreshTokenRevocationServiceMock);
        staffAccountLockedEvent = new StaffAccountLockedEvent(StaffAccountFixture.validStaffAccount());
    }

    @Test
    void shouldCallRevocationServiceWithCorrectStaffAccountId() {
        // Given
        ArgumentCaptor<StaffAccountId> staffAccountIdArgumentCaptor = ArgumentCaptor.forClass(StaffAccountId.class);

        // When
        sut.handle(staffAccountLockedEvent);

        // Then
        verify(refreshTokenRevocationServiceMock, times(1))
                .revokeAllTokensForStaffAccount(staffAccountIdArgumentCaptor.capture());

        StaffAccountId staffAccountId = staffAccountIdArgumentCaptor.getValue();
        assertThat(staffAccountId).isEqualTo(staffAccountLockedEvent.getStaffAccountId());
    }

    @Test
    void shouldCatchDomainException() {
        // Given
        doThrow(DomainException.class)
                .when(refreshTokenRevocationServiceMock)
                .revokeAllTokensForStaffAccount(any(StaffAccountId.class));

        // When & Then
        assertThatNoException()
                .isThrownBy(() -> sut.handle(staffAccountLockedEvent));
    }

    @Test
    void shouldCatchInfraException() {
        // Given
        doThrow(InfraException.class)
                .when(refreshTokenRevocationServiceMock)
                .revokeAllTokensForStaffAccount(any(StaffAccountId.class));

        // When & Then
        assertThatNoException()
                .isThrownBy(() -> sut.handle(staffAccountLockedEvent));
    }
}
