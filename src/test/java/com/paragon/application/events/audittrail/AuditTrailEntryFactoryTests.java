package com.paragon.application.events.audittrail;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.AuditEntryTargetType;
import com.paragon.domain.events.staffaccountevents.StaffAccountEventBase;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountLoggedInEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountRegisteredEvent;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class AuditTrailEntryFactoryTests {

    @ParameterizedTest
    @MethodSource("provideStaffAccountEvents")
    void shouldCreateCorrectAuditTrailEntryForEvent(
            StaffAccountEventBase event,
            StaffAccountId expectedActorId,
            AuditEntryActionType expectedActionType,
            String expectedTargetId,
            AuditEntryTargetType expectedTargetType
    ) {
        // When
        AuditTrailEntry result = AuditTrailEntryFactory.fromStaffAccountEvent(event);

        // Then
        assertThat(result.getActorId()).isEqualTo(expectedActorId);
        assertThat(result.getActionType()).isEqualTo(expectedActionType);
        assertThat(result.getTargetId().getValue()).isEqualTo(expectedTargetId);
        assertThat(result.getTargetType()).isEqualTo(expectedTargetType);
    }

    private static Stream<Arguments> provideStaffAccountEvents() {
        String creatorId = UUID.randomUUID().toString();
        String registeredAccountId = UUID.randomUUID().toString();
        String lockedAccountId = UUID.randomUUID().toString();
        String loggedInAccountId = UUID.randomUUID().toString();

        // Create a creator account for REGISTERED events
        StaffAccount creator = new StaffAccountFixture()
                .withId(creatorId)
                .build();

        // Create the account being registered
        StaffAccount registeredAccount = new StaffAccountFixture()
                .withId(registeredAccountId)
                .withCreatedBy(creatorId)
                .build();

        // Create account for LOCKED event
        StaffAccount lockedAccount = new StaffAccountFixture()
                .withId(lockedAccountId)
                .build();

        // Create account for LOGGED_IN event
        StaffAccount loggedInAccount = new StaffAccountFixture()
                .withId(loggedInAccountId)
                .build();

        return Stream.of(
                // STAFF_ACCOUNT_REGISTERED: actor is creator, target is new account
                arguments(
                        new StaffAccountRegisteredEvent(registeredAccount),
                        StaffAccountId.from(creatorId),
                        AuditEntryActionType.REGISTER_ACCOUNT,
                        registeredAccountId,
                        AuditEntryTargetType.ACCOUNT
                ),

                // STAFF_ACCOUNT_LOCKED: actor is account itself, target is account itself
                arguments(
                        new StaffAccountLockedEvent(lockedAccount),
                        StaffAccountId.from(lockedAccountId),
                        AuditEntryActionType.ACCOUNT_LOCKED,
                        lockedAccountId,
                        AuditEntryTargetType.ACCOUNT
                ),

                // STAFF_ACCOUNT_LOGGED_IN: actor is account itself, target is account itself
                arguments(
                        new StaffAccountLoggedInEvent(loggedInAccount),
                        StaffAccountId.from(loggedInAccountId),
                        AuditEntryActionType.LOGIN,
                        loggedInAccountId,
                        AuditEntryTargetType.ACCOUNT
                )
        );
    }
}