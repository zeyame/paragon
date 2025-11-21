package com.paragon.application.events.refreshtokens;

import com.paragon.application.services.StaffAccountRefreshTokenRevocationService;
import com.paragon.domain.events.staffaccountevents.StaffAccountDisabledEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountEventBase;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountPasswordResetEvent;
import com.paragon.domain.interfaces.RefreshTokenWriteRepo;
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
    private final StaffAccountRefreshTokenRevocationService staffAccountRefreshTokenRevocationServiceMock;

    StaffAccountRefreshTokenRevocationHandlerTests() {
        staffAccountRefreshTokenRevocationServiceMock = mock(StaffAccountRefreshTokenRevocationService.class);
        sut = new StaffAccountRefreshTokenRevocationHandler(staffAccountRefreshTokenRevocationServiceMock);
    }

    @ParameterizedTest
    @MethodSource("provideEvents")
    void shouldCallRevocationService(StaffAccountEventBase event) {
        // When
        sut.handle(event);

        // Then
        verify(staffAccountRefreshTokenRevocationServiceMock, times(1))
                .revokeAllTokensForStaffAccount(event.getStaffAccountId());
    }

    private static Stream<Arguments> provideEvents() {
        return Stream.of(
                Arguments.of(new StaffAccountLockedEvent(StaffAccountFixture.validStaffAccount())),
                Arguments.of(new StaffAccountDisabledEvent(StaffAccountFixture.validStaffAccount())),
                Arguments.of(new StaffAccountPasswordResetEvent(StaffAccountFixture.validStaffAccount()))
        );
    }
}
