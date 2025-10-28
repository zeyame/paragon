package com.paragon.integration.events;

import com.paragon.application.events.EventBusImpl;
import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountLoggedInEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountRegisteredEvent;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryTargetId;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountEventAuditTrailHandlerTests extends IntegrationTestBase {
    private final EventBusImpl eventBus;
    private final TestJdbcHelper jdbcHelper;

    @Autowired
    public StaffAccountEventAuditTrailHandlerTests(EventBusImpl eventBus, WriteJdbcHelper writeJdbcHelper) {
        this.eventBus = eventBus;
        this.jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
    }

    @Test
    void shouldPersistAuditTrailEntryForStaffAccountRegisteredEvent() {
        // Given
        StaffAccountRegisteredEvent staffAccountRegisteredEvent = new StaffAccountRegisteredEvent(
                new StaffAccountFixture()
                        .withCreatedBy(adminId)
                        .build()
        );

        // When
        eventBus.publishAll(List.of(staffAccountRegisteredEvent));

        // Then
        List<AuditTrailEntry> auditTrailEntries = jdbcHelper.getAuditTrailEntriesByActorAndAction(staffAccountRegisteredEvent.getStaffAccountCreatedBy(), AuditEntryActionType.REGISTER_ACCOUNT);
        assertThat(auditTrailEntries).hasSize(1);

        AuditTrailEntry staffAccountRegisteredEntry = auditTrailEntries.getFirst();
        assertThat(staffAccountRegisteredEntry.getActorId()).isEqualTo(staffAccountRegisteredEvent.getStaffAccountCreatedBy());
        assertThat(staffAccountRegisteredEntry.getActionType()).isEqualTo(AuditEntryActionType.REGISTER_ACCOUNT);
        assertThat(staffAccountRegisteredEntry.getTargetId()).isEqualTo(AuditEntryTargetId.of(staffAccountRegisteredEvent.getStaffAccountId().getValue().toString()));
        assertThat(staffAccountRegisteredEntry.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }

    @Test
    void shouldPersistAuditTrailEntryForStaffAccountLockedEvent() {
        // Given
        StaffAccountLockedEvent staffAccountLockedEvent = new StaffAccountLockedEvent(
                new StaffAccountFixture()
                        .withId(adminId)
                        .build()
        );

        // When
        eventBus.publishAll(List.of(staffAccountLockedEvent));

        // Then
        List<AuditTrailEntry> auditTrailEntries = jdbcHelper.getAuditTrailEntriesByActorAndAction(staffAccountLockedEvent.getStaffAccountId(), AuditEntryActionType.ACCOUNT_LOCKED);
        assertThat(auditTrailEntries).hasSize(1);

        AuditTrailEntry staffAccountLockedEntry = auditTrailEntries.getFirst();
        assertThat(staffAccountLockedEntry.getActorId()).isEqualTo(staffAccountLockedEvent.getStaffAccountId());
        assertThat(staffAccountLockedEntry.getActionType()).isEqualTo(AuditEntryActionType.ACCOUNT_LOCKED);
        assertThat(staffAccountLockedEntry.getTargetId()).isEqualTo(AuditEntryTargetId.of(staffAccountLockedEvent.getStaffAccountId().getValue().toString()));
        assertThat(staffAccountLockedEntry.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }

    @Test
    void shouldPersistAuditTrailEntryForStaffAccountLoggedInEvent() {
        // Given
        StaffAccountLoggedInEvent staffAccountLoggedInEvent = new StaffAccountLoggedInEvent(
                new StaffAccountFixture()
                        .withId(adminId)
                        .build()
        );

        // When
        eventBus.publishAll(List.of(staffAccountLoggedInEvent));

        // Then
        List<AuditTrailEntry> auditTrailEntries = jdbcHelper.getAuditTrailEntriesByActorAndAction(staffAccountLoggedInEvent.getStaffAccountId(), AuditEntryActionType.LOGIN);
        assertThat(auditTrailEntries).hasSize(1);

        AuditTrailEntry staffAccountLoggedInEntry = auditTrailEntries.getFirst();
        assertThat(staffAccountLoggedInEntry.getActorId()).isEqualTo(staffAccountLoggedInEvent.getStaffAccountId());
        assertThat(staffAccountLoggedInEntry.getActionType()).isEqualTo(AuditEntryActionType.LOGIN);
        assertThat(staffAccountLoggedInEntry.getTargetId()).isEqualTo(AuditEntryTargetId.of(staffAccountLoggedInEvent.getStaffAccountId().getValue().toString()));
        assertThat(staffAccountLoggedInEntry.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }
}
