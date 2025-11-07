package com.paragon.integration.events;

import com.paragon.application.events.EventBusImpl;
import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.staffaccountevents.*;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryTargetId;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

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

    @Test
    void shouldPersistAuditTrailEntryForStaffAccountDisabledEvent() {
        // Given
        StaffAccount disabledStaffAccount = new StaffAccountFixture()
                .withId(UUID.randomUUID().toString())
                .withDisabledBy(adminId)
                .withStatus(StaffAccountStatus.DISABLED)
                .build();
        StaffAccountDisabledEvent staffAccountDisabledEvent = new StaffAccountDisabledEvent(disabledStaffAccount);

        // When
        eventBus.publishAll(List.of(staffAccountDisabledEvent));

        // Then
        List<AuditTrailEntry> auditTrailEntries = jdbcHelper.getAuditTrailEntriesByActorAndAction(
                staffAccountDisabledEvent.getStaffAccountDisabledBy(), AuditEntryActionType.DISABLE_ACCOUNT
        );
        assertThat(auditTrailEntries).hasSize(1);

        AuditTrailEntry disabledEntry = auditTrailEntries.getFirst();
        assertThat(disabledEntry.getActorId()).isEqualTo(staffAccountDisabledEvent.getStaffAccountDisabledBy());
        assertThat(disabledEntry.getActionType()).isEqualTo(AuditEntryActionType.DISABLE_ACCOUNT);
        assertThat(disabledEntry.getTargetId()).isEqualTo(AuditEntryTargetId.of(staffAccountDisabledEvent.getStaffAccountId().getValue().toString()));
        assertThat(disabledEntry.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }

    @Test
    void shouldPersistAuditTrailEntryForStaffAccountPasswordResetEvent() {
        // Given
        StaffAccount staffAccount = new StaffAccountFixture()
                .withPasswordResetBy(adminId)
                .build();
        StaffAccountPasswordResetEvent passwordResetEvent = new StaffAccountPasswordResetEvent(staffAccount);

        // When
        eventBus.publishAll(List.of(passwordResetEvent));

        // Then
        List<AuditTrailEntry> auditTrailEntries = jdbcHelper.getAuditTrailEntriesByActorAndAction(
                passwordResetEvent.getStaffAccountPasswordResetBy(), AuditEntryActionType.RESET_ACCOUNT_PASSWORD
        );
        assertThat(auditTrailEntries).hasSize(1);

        AuditTrailEntry passwordResetEntry = auditTrailEntries.getFirst();
        assertThat(passwordResetEntry.getActorId()).isEqualTo(passwordResetEvent.getStaffAccountPasswordResetBy());
        assertThat(passwordResetEntry.getActionType()).isEqualTo(AuditEntryActionType.RESET_ACCOUNT_PASSWORD);
        assertThat(passwordResetEntry.getTargetId()).isEqualTo(AuditEntryTargetId.of(passwordResetEvent.getStaffAccountId().getValue().toString()));
        assertThat(passwordResetEntry.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }
}
