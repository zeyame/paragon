package com.paragon.application.events.audittrail;

import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.interfaces.repos.AuditTrailWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StaffAccountEventAuditTrailHandlerTests {
    private final StaffAccountEventAuditTrailHandler sut;
    private final AuditTrailWriteRepo auditTrailWriteRepoMock;

    StaffAccountEventAuditTrailHandlerTests() {
        auditTrailWriteRepoMock = mock(AuditTrailWriteRepo.class);
        sut = new StaffAccountEventAuditTrailHandler(auditTrailWriteRepoMock);
    }

    @Test
    void shouldCallRepoCreateWithAuditTrailEntry() {
        // Given
        StaffAccount staffAccount = new StaffAccountFixture()
                .withId(UUID.randomUUID().toString())
                .build();
        StaffAccountLockedEvent event = new StaffAccountLockedEvent(staffAccount);

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
}
