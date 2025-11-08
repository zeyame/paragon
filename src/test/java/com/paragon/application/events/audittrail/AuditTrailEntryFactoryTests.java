package com.paragon.application.events.audittrail;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.staffaccountevents.*;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditTrailEntryFactoryTests {

    @Test
    void shouldCreateAuditTrailEntryForStaffAccountRegisteredEvent() {
        // Given
        String creatorId = UUID.randomUUID().toString();
        String accountId = UUID.randomUUID().toString();

        StaffAccount account = new StaffAccountFixture()
                .withId(accountId)
                .withCreatedBy(creatorId)
                .build();

        StaffAccountRegisteredEvent event = new StaffAccountRegisteredEvent(account);

        // When
        AuditTrailEntry result = AuditTrailEntryFactory.fromStaffAccountEvent(event);

        // Then
        assertThat(result.getActorId().getValue().toString()).isEqualTo(creatorId);
        assertThat(result.getActionType()).isEqualTo(AuditEntryActionType.REGISTER_ACCOUNT);
        assertThat(result.getTargetId().getValue()).isEqualTo(accountId);
        assertThat(result.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }

    @Test
    void shouldCreateAuditTrailEntryForStaffAccountLockedEvent() {
        // Given
        String accountId = UUID.randomUUID().toString();

        StaffAccount account = new StaffAccountFixture()
                .withId(accountId)
                .build();

        StaffAccountLockedEvent event = new StaffAccountLockedEvent(account);

        // When
        AuditTrailEntry result = AuditTrailEntryFactory.fromStaffAccountEvent(event);

        // Then
        assertThat(result.getActorId().getValue().toString()).isEqualTo(accountId);
        assertThat(result.getActionType()).isEqualTo(AuditEntryActionType.ACCOUNT_LOCKED);
        assertThat(result.getTargetId().getValue()).isEqualTo(accountId);
        assertThat(result.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }

    @Test
    void shouldCreateAuditTrailEntryForStaffAccountLoggedInEvent() {
        // Given
        String accountId = UUID.randomUUID().toString();

        StaffAccount account = new StaffAccountFixture()
                .withId(accountId)
                .build();

        StaffAccountLoggedInEvent event = new StaffAccountLoggedInEvent(account);

        // When
        AuditTrailEntry result = AuditTrailEntryFactory.fromStaffAccountEvent(event);

        // Then
        assertThat(result.getActorId().getValue().toString()).isEqualTo(accountId);
        assertThat(result.getActionType()).isEqualTo(AuditEntryActionType.LOGIN);
        assertThat(result.getTargetId().getValue()).isEqualTo(accountId);
        assertThat(result.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }

    @Test
    void shouldCreateAuditTrailEntryForStaffAccountDisabledEvent() {
        // Given
        String disablerId = UUID.randomUUID().toString();
        String accountId = UUID.randomUUID().toString();

        StaffAccount account = new StaffAccountFixture()
                .withId(accountId)
                .withStatus(StaffAccountStatus.DISABLED)
                .withDisabledBy(disablerId)
                .build();

        StaffAccountDisabledEvent event = new StaffAccountDisabledEvent(account);

        // When
        AuditTrailEntry result = AuditTrailEntryFactory.fromStaffAccountEvent(event);

        // Then
        assertThat(result.getActorId().getValue().toString()).isEqualTo(disablerId);
        assertThat(result.getActionType()).isEqualTo(AuditEntryActionType.DISABLE_ACCOUNT);
        assertThat(result.getTargetId().getValue()).isEqualTo(accountId);
        assertThat(result.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }

    @Test
    void shouldCreateAuditTrailEntryForStaffAccountPasswordResetEvent() {
        // Given
        String resetById = UUID.randomUUID().toString();
        String accountId = UUID.randomUUID().toString();

        StaffAccount account = new StaffAccountFixture()
                .withId(accountId)
                .withPasswordResetBy(resetById)
                .build();

        StaffAccountPasswordResetEvent event = new StaffAccountPasswordResetEvent(account);

        // When
        AuditTrailEntry result = AuditTrailEntryFactory.fromStaffAccountEvent(event);

        // Then
        assertThat(result.getActorId().getValue().toString()).isEqualTo(resetById);
        assertThat(result.getActionType()).isEqualTo(AuditEntryActionType.RESET_ACCOUNT_PASSWORD);
        assertThat(result.getTargetId().getValue()).isEqualTo(accountId);
        assertThat(result.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }

    @Test
    void shouldCreateAuditTrailEntryForStaffAccountEnabledEvent() {
        // Given
        String enabledById = UUID.randomUUID().toString();
        String accountId = UUID.randomUUID().toString();

        StaffAccount account = new StaffAccountFixture()
                .withId(accountId)
                .withEnabledBy(enabledById)
                .build();

        StaffAccountEnabledEvent event = new StaffAccountEnabledEvent(account);

        // When
        AuditTrailEntry auditTrailEntry = AuditTrailEntryFactory.fromStaffAccountEvent(event);

        // Then
        assertThat(auditTrailEntry.getActorId().getValue().toString()).isEqualTo(enabledById);
        assertThat(auditTrailEntry.getActionType()).isEqualTo(AuditEntryActionType.ENABLE_ACCOUNT);
        assertThat(auditTrailEntry.getTargetId().getValue()).isEqualTo(accountId);
        assertThat(auditTrailEntry.getTargetType()).isEqualTo(AuditEntryTargetType.ACCOUNT);
    }
}