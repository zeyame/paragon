package com.paragon.application.events.audittrail;

import com.paragon.domain.events.staffaccountevents.*;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repositories.AuditTrailWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StaffAccountEventAuditTrailHandlerTests {
    private final StaffAccountEventAuditTrailHandler sut;
    private final AuditTrailWriteRepo auditTrailWriteRepoMock;

    StaffAccountEventAuditTrailHandlerTests() {
        auditTrailWriteRepoMock = mock(AuditTrailWriteRepo.class);
        sut = new StaffAccountEventAuditTrailHandler(auditTrailWriteRepoMock);
    }

    @ParameterizedTest
    @MethodSource("provideEvents")
    void shouldCallRepoCreateWithAuditTrailEntry(StaffAccountEventBase event) {
        // When
        sut.handle(event);

        // Then
        verify(auditTrailWriteRepoMock, times(1))
                .create(any(AuditTrailEntry.class));
    }

    @Test
    void shouldCatchDomainException() {
        // Given
        StaffAccount staffAccount = new StaffAccountFixture()
                .withId(UUID.randomUUID().toString())
                .build();
        StaffAccountLockedEvent event = new StaffAccountLockedEvent(staffAccount);

        doThrow(DomainException.class)
                .when(auditTrailWriteRepoMock)
                .create(any(AuditTrailEntry.class));

        // When & Then
        assertThatNoException()
                .isThrownBy(() -> sut.handle(event));
    }

    @Test
    void shouldCatchInfraException() {
        // Given
        StaffAccount staffAccount = new StaffAccountFixture()
                .withId(UUID.randomUUID().toString())
                .build();
        StaffAccountLockedEvent event = new StaffAccountLockedEvent(staffAccount);

        doThrow(InfraException.class)
                .when(auditTrailWriteRepoMock)
                .create(any(AuditTrailEntry.class));

        // When & Then
        assertThatNoException()
                .isThrownBy(() -> sut.handle(event));
    }

    private static Stream<Arguments> provideEvents() {
        return Stream.of(
                arguments(new StaffAccountRegisteredEvent(StaffAccountFixture.validStaffAccount())),
                arguments(new StaffAccountLoggedInEvent(StaffAccountFixture.validStaffAccount())),
                arguments(new StaffAccountLockedEvent(StaffAccountFixture.validStaffAccount())),
                arguments(new StaffAccountDisabledEvent(new StaffAccountFixture()
                        .withDisabledBy(UUID.randomUUID().toString())
                        .build()
                )),
                arguments(new StaffAccountPasswordResetEvent(new StaffAccountFixture()
                        .withPasswordResetBy(UUID.randomUUID().toString())
                        .build()
                )),
                arguments(new StaffAccountEnabledEvent(new StaffAccountFixture()
                        .withEnabledBy(UUID.randomUUID().toString())
                        .build()
                )),
                arguments(new StaffAccountPasswordChangedEvent(StaffAccountFixture.validStaffAccount()))
        );
    }
}
