package com.paragon.application.events.audittrail;

import com.paragon.application.context.RequestMetadataProvider;
import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.enums.Outcome;
import com.paragon.domain.events.staffaccountevents.StaffAccountLoggedInEvent;
import com.paragon.domain.interfaces.repos.AuditTrailWriteRepo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class StaffAccountLoggedInEventAuditHandlerTests {
    private final StaffAccountLoggedInEventAuditHandler sut;
    private final AuditTrailWriteRepo auditTrailWriteRepoMock;
    private final RequestMetadataProvider requestMetadataProviderMock;
    private final StaffAccountLoggedInEvent staffAccountLoggedInEvent;
    private final String ipAddress;
    private final String correlationId;

    StaffAccountLoggedInEventAuditHandlerTests() {
        auditTrailWriteRepoMock = mock(AuditTrailWriteRepo.class);
        requestMetadataProviderMock = mock(RequestMetadataProvider.class);
        staffAccountLoggedInEvent = new StaffAccountLoggedInEvent(StaffAccountFixture.validStaffAccount());
        ipAddress = "ip-address";
        correlationId = "correlation-id";
        sut = new StaffAccountLoggedInEventAuditHandler(auditTrailWriteRepoMock, requestMetadataProviderMock);

        when(requestMetadataProviderMock.getIpAddress()).thenReturn(ipAddress);
        when(requestMetadataProviderMock.getCorrelationId()).thenReturn(correlationId);
    }

    @Test
    void handle_WhenStaffAccountLoggedInEvent_ShouldCallAuditTrailWriteRepoWithCorrectAuditTrailEntry() {
        // Given
        ArgumentCaptor<AuditTrailEntry> auditTrailEntryCaptor = ArgumentCaptor.forClass(AuditTrailEntry.class);

        // When
        sut.handle(staffAccountLoggedInEvent);

        // Then
        verify(auditTrailWriteRepoMock, times(1)).create(auditTrailEntryCaptor.capture());

        AuditTrailEntry auditTrailEntry = auditTrailEntryCaptor.getValue();
        assertThat(auditTrailEntry.getActorId()).isEqualTo(staffAccountLoggedInEvent.getStaffAccountId());
        assertThat(auditTrailEntry.getActionType()).isEqualTo(AuditEntryActionType.LOGIN);
        assertThat(auditTrailEntry.getTargetId().getValue()).isEqualTo(staffAccountLoggedInEvent.getStaffAccountId().getValue().toString());
        assertThat(auditTrailEntry.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
        assertThat(auditTrailEntry.getOutcome()).isEqualTo(Outcome.SUCCESS);
        assertThat(auditTrailEntry.getIpAddress()).isEqualTo(ipAddress);
        assertThat(auditTrailEntry.getCorrelationId()).isEqualTo(correlationId);
    }
}