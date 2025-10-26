package com.paragon.application.events.audittrail;

import com.paragon.application.common.interfaces.RequestMetadataProvider;
import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.enums.Outcome;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.interfaces.repos.AuditTrailWriteRepo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class StaffAccountLockedEventAuditHandlerTests {
    private final StaffAccountLockedEventAuditHandler sut;
    private final AuditTrailWriteRepo auditTrailWriteRepoMock;
    private final RequestMetadataProvider requestMetadataProviderMock;
    private final StaffAccountLockedEvent staffAccountLockedEvent;
    private final String ipAddress;
    private final String correlationId;

    StaffAccountLockedEventAuditHandlerTests() {
        auditTrailWriteRepoMock = mock(AuditTrailWriteRepo.class);
        requestMetadataProviderMock = mock(RequestMetadataProvider.class);
        staffAccountLockedEvent = new StaffAccountLockedEvent(StaffAccountFixture.validStaffAccount());
        ipAddress = "ip-address";
        correlationId = "correlation-id";
        sut = new StaffAccountLockedEventAuditHandler(auditTrailWriteRepoMock, requestMetadataProviderMock);

        when(requestMetadataProviderMock.getIpAddress()).thenReturn(ipAddress);
        when(requestMetadataProviderMock.getCorrelationId()).thenReturn(correlationId);
    }

    @Test
    void handle_WhenStaffAccountLoggedInEvent_ShouldCallAuditTrailWriteRepoWithCorrectAuditTrailEntry() {
        // Given
        ArgumentCaptor<AuditTrailEntry> auditTrailEntryCaptor = ArgumentCaptor.forClass(AuditTrailEntry.class);

        // When
        sut.handle(staffAccountLockedEvent);

        // Then
        verify(auditTrailWriteRepoMock, times(1)).create(auditTrailEntryCaptor.capture());

        AuditTrailEntry auditTrailEntry = auditTrailEntryCaptor.getValue();
        assertThat(auditTrailEntry.getActorId()).isEqualTo(staffAccountLockedEvent.getStaffAccountId());
        assertThat(auditTrailEntry.getActionType()).isEqualTo(AuditEntryActionType.ACCOUNT_LOCKED);
        assertThat(auditTrailEntry.getTargetId().getValue()).isEqualTo(staffAccountLockedEvent.getStaffAccountId().getValue().toString());
        assertThat(auditTrailEntry.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
        assertThat(auditTrailEntry.getOutcome()).isEqualTo(Outcome.SUCCESS);
        assertThat(auditTrailEntry.getIpAddress()).isEqualTo(ipAddress);
        assertThat(auditTrailEntry.getCorrelationId()).isEqualTo(correlationId);
    }
}
