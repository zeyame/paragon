package com.paragon.domain.models.aggregates;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.events.staffaccountevents.*;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class StaffAccountTests {
    @Nested
    class Register {
        private final Username username;
        private final Email email;
        private final Password password;
        private final OrderAccessDuration orderAccessDuration;
        private final ModmailTranscriptAccessDuration modmailTranscriptAccessDuration;
        private final StaffAccountId createdBy;
        private final List<PermissionCode> permissionCodes;

        Register() {
            username = Username.of("john_doe");
            email = Email.of("john_doe@example.com");
            password = Password.fromHashed("$argon2id$v=19$m=65536,t=3,p=1$QWxhZGRpbjpPcGVuU2VzYW1l$2iYvT1yzFzHtXJH7zM4jW1Z2sK7Tg==");
            orderAccessDuration = OrderAccessDuration.from(5);
            modmailTranscriptAccessDuration = ModmailTranscriptAccessDuration.from(10);
            createdBy = StaffAccountId.generate();
            permissionCodes = List.of(SystemPermissions.MANAGE_ACCOUNTS, SystemPermissions.APPROVE_PASSWORD_CHANGE);
        }

        @Test
        void givenValidInputWithoutEmail_shouldRegisterStaffAccount() {
            // When
            StaffAccount staffAccount = StaffAccount.register(username, null, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionCodes);

            // Then
            assertThat(staffAccount).isNotNull();
            assertThat(staffAccount.getUsername()).isEqualTo(username);
            assertThat(staffAccount.getEmail()).isNull();
            assertThat(staffAccount.getPassword()).isEqualTo(password);
            assertThat(staffAccount.isPasswordTemporary()).isTrue();
            assertThat(staffAccount.getPasswordIssuedAt()).isNotNull();
            assertThat(staffAccount.getOrderAccessDuration()).isEqualTo(orderAccessDuration);
            assertThat(staffAccount.getModmailTranscriptAccessDuration()).isEqualTo(modmailTranscriptAccessDuration);
            assertThat(staffAccount.getStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
            assertThat(staffAccount.getFailedLoginAttempts().getValue()).isZero();
            assertThat(staffAccount.getLockedUntil()).isNull();
            assertThat(staffAccount.getLastLoginAt()).isNull();
            assertThat(staffAccount.getCreatedBy()).isEqualTo(createdBy);
            assertThat(staffAccount.getDisabledBy()).isNull();
            assertThat(staffAccount.getEnabledBy()).isNull();
            assertThat(staffAccount.getPasswordResetBy()).isNull();
            assertThat(staffAccount.getPermissionCodes()).isEqualTo(permissionCodes);
            assertThat(staffAccount.getVersion().getValue()).isEqualTo(1);
        }

        @Test
        void givenValidInputWithEmail_shouldRegisterStaffAccount() {
            // When
            StaffAccount staffAccount = StaffAccount.register(username, email, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionCodes);

            // Then
            assertThat(staffAccount).isNotNull();
            assertThat(staffAccount.getUsername()).isEqualTo(username);
            assertThat(staffAccount.getEmail()).isEqualTo(email);
            assertThat(staffAccount.getPassword()).isEqualTo(password);
            assertThat(staffAccount.isPasswordTemporary()).isTrue();
            assertThat(staffAccount.getPasswordIssuedAt()).isNotNull();
            assertThat(staffAccount.getOrderAccessDuration()).isEqualTo(orderAccessDuration);
            assertThat(staffAccount.getModmailTranscriptAccessDuration()).isEqualTo(modmailTranscriptAccessDuration);
            assertThat(staffAccount.getStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
            assertThat(staffAccount.getFailedLoginAttempts().getValue()).isZero();
            assertThat(staffAccount.getLockedUntil()).isNull();
            assertThat(staffAccount.getLastLoginAt()).isNull();
            assertThat(staffAccount.getCreatedBy()).isEqualTo(createdBy);
            assertThat(staffAccount.getDisabledBy()).isNull();
            assertThat(staffAccount.getEnabledBy()).isNull();
            assertThat(staffAccount.getPasswordResetBy()).isNull();
            assertThat(staffAccount.getPermissionCodes()).isEqualTo(permissionCodes);
            assertThat(staffAccount.getVersion().getValue()).isEqualTo(1);
        }

        @Test
        void shouldGenerateUniqueStaffAccountId() {
            // When
            StaffAccount staffAccount1 = StaffAccount.register(username, email, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionCodes);
            StaffAccount staffAccount2 = StaffAccount.register(username, email, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionCodes);

            // Then
            assertThat(staffAccount1.getId()).isNotNull();
            assertThat(staffAccount2.getId()).isNotNull();
            assertThat(staffAccount1.getId()).isNotEqualTo(staffAccount2.getId());
        }

        @Test
        void shouldEnqueueStaffAccountRegisteredEvent_uponSuccessfulRegistration() {
            // When
            StaffAccount staffAccount = StaffAccount.register(username, email, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionCodes);

            // Then
            List<DomainEvent> queuedEvents = staffAccount.dequeueUncommittedEvents();
            assertThat(queuedEvents).hasSize(1);
            assertThat(queuedEvents.getFirst()).isInstanceOf(StaffAccountRegisteredEvent.class);

            StaffAccountRegisteredEvent event = (StaffAccountRegisteredEvent) queuedEvents.getFirst();
            assertThatEventDataIsCorrect(event, staffAccount);
        }

        @Test
        void givenMissingUsername_registrationShouldFail() {
            // Given
            String expectedErrorMessage = StaffAccountExceptionInfo.usernameRequired().getMessage();
            int expectedErrorCode = StaffAccountExceptionInfo.usernameRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> StaffAccount.register(null, email, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionCodes))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenMissingPassword_registrationShouldFail() {
            // Given
            String expectedErrorMessage = StaffAccountExceptionInfo.passwordRequired().getMessage();
            int expectedErrorCode = StaffAccountExceptionInfo.passwordRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> StaffAccount.register(username, email, null, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionCodes))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenMissingOrderAccessDuration_registrationShouldFail() {
            // Given
            String expectedErrorMessage = StaffAccountExceptionInfo.orderAccessDurationRequired().getMessage();
            int expectedErrorCode = StaffAccountExceptionInfo.orderAccessDurationRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> StaffAccount.register(username, email, password, null, modmailTranscriptAccessDuration, createdBy, permissionCodes))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenMissingModmailTranscriptAccessDuration_registrationShouldFail() {
            // Given
            String expectedErrorMessage = StaffAccountExceptionInfo.modmailTranscriptAccessDurationRequired().getMessage();
            int expectedErrorCode = StaffAccountExceptionInfo.modmailTranscriptAccessDurationRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> StaffAccount.register(username, email, password, orderAccessDuration, null, createdBy, permissionCodes))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenMissingCreatedByStaffAccountId_registrationShouldFail() {
            // Given
            String expectedErrorMessage = StaffAccountExceptionInfo.createdByRequired().getMessage();
            int expectedErrorCode = StaffAccountExceptionInfo.createdByRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> StaffAccount.register(username, email, password, orderAccessDuration, modmailTranscriptAccessDuration, null, permissionCodes))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenNoPermissionsAssigned_registrationShouldFail() {
            // Given
            String expectedErrorMessage = StaffAccountExceptionInfo.atLeastOnePermissionRequired().getMessage();
            int expectedErrorCode = StaffAccountExceptionInfo.atLeastOnePermissionRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> StaffAccount.register(username, email, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, List.of()))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }

    @Nested
    class RegisterFailedLoginAttempt {
        @Test
        void shouldIncrementFailedLoginAttempts() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();

            // When
            staffAccount.registerFailedLoginAttempt();

            // Then
            assertThat(staffAccount.getFailedLoginAttempts().getValue()).isEqualTo(1);
        }

        @Test
        void shouldIncreaseVersion() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();

            // When
            staffAccount.registerFailedLoginAttempt();

            // Then
            assertThat(staffAccount.getVersion().getValue()).isEqualTo(2);
        }

        @Test
        void locksAccountForFifteenMinutes_whenMaximumFailedLoginAttemptsAreReached() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withFailedLoginAttempts(4)
                    .build();

            // When
            staffAccount.registerFailedLoginAttempt();

            // Then
            assertThat(staffAccount.getStatus()).isEqualTo(StaffAccountStatus.LOCKED);
            assertThat(staffAccount.getLockedUntil())
                    .isNotNull()
                    .isBetween(Instant.now(), Instant.now().plus(Duration.ofMinutes(16)));
        }

        @Test
        void shouldEnqueueStaffAccountLockedEvent_whenAccountGetsLockedUponFinalLoginAttempt() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withFailedLoginAttempts(4)
                    .build();

            // When
            staffAccount.registerFailedLoginAttempt();

            // Then
            List<DomainEvent> enqueuedEvents = staffAccount.dequeueUncommittedEvents();
            assertThat(enqueuedEvents).hasSize(1);
            assertThat(enqueuedEvents.getFirst()).isInstanceOf(StaffAccountLockedEvent.class);

            StaffAccountLockedEvent event = (StaffAccountLockedEvent) enqueuedEvents.getFirst();
            assertThatEventDataIsCorrect(event, staffAccount);
        }
    }

    @Nested
    class Login {
        @Test
        void shouldLoginSuccessfully() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();

            // When
            staffAccount.login();

            // Then
            assertThat(staffAccount.getFailedLoginAttempts().getValue()).isZero();
            assertThat(staffAccount.getVersion().getValue()).isEqualTo(2);
            assertThat(staffAccount.getLastLoginAt()).isNotNull();
            assertThat(staffAccount.getLastLoginAt()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        void shouldEnqueueStaffAccountLoggedInEvent() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();

            // When
            staffAccount.login();

            // Then
            List<DomainEvent> queuedEvents = staffAccount.dequeueUncommittedEvents();
            assertThat(queuedEvents).hasSize(1);
            assertThat(queuedEvents.getFirst()).isInstanceOf(StaffAccountLoggedInEvent.class);

            StaffAccountLoggedInEvent loggedInEvent = (StaffAccountLoggedInEvent) queuedEvents.getFirst();
            assertThatEventDataIsCorrect(loggedInEvent, staffAccount);
        }

        @Test
        void shouldThrowStaffAccountException_whenAccountIsDisabled() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withStatus(StaffAccountStatus.DISABLED)
                    .build();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(staffAccount::login)
                    .extracting("message", "domainErrorCode")
                    .containsExactly(StaffAccountExceptionInfo.loginFailedAccountDisabled().getMessage(), StaffAccountExceptionInfo.loginFailedAccountDisabled().getDomainErrorCode());
        }

        @Test
        void shouldThrowStaffAccountException_whenAccountIsLocked() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withStatus(StaffAccountStatus.LOCKED)
                    .withLockedUntil(Instant.now().plus(Duration.ofMinutes(15)))
                    .build();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(staffAccount::login)
                    .extracting("message", "domainErrorCode")
                    .containsExactly(StaffAccountExceptionInfo.loginFailedAccountLocked().getMessage(), StaffAccountExceptionInfo.loginFailedAccountLocked().getDomainErrorCode());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldUnlockAccountAndRevertToCorrectStatus_whenLockDurationHasExpired(boolean isPasswordTemporary) {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withPasswordTemporary(isPasswordTemporary)
                    .withStatus(StaffAccountStatus.LOCKED)
                    .withLockedUntil(Instant.now().minus(Duration.ofMinutes(1))) // expired
                    .build();

            // When
            staffAccount.login();

            // Then
            assertThat(staffAccount.getStatus()).isEqualTo(isPasswordTemporary ? StaffAccountStatus.PENDING_PASSWORD_CHANGE : StaffAccountStatus.ACTIVE);
            assertThat(staffAccount.getLockedUntil()).isNull();
            assertThat(staffAccount.getLastLoginAt()).isNotNull();
        }
    }

    @Nested
    class Disable {
        @Test
        void shouldDisableStaffAccount() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();
            StaffAccountId disabledBy = StaffAccountId.generate();

            // When
            staffAccount.disable(disabledBy);

            // Then
            assertThat(staffAccount.getStatus()).isEqualTo(StaffAccountStatus.DISABLED);
            assertThat(staffAccount.getDisabledBy()).isEqualTo(disabledBy);
            assertThat(staffAccount.getEnabledBy()).isNull();
            assertThat(staffAccount.getVersion()).isEqualTo(Version.of(2));
        }

        @Test
        void shouldEnqueueStaffAccountDisabledEvent() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();
            StaffAccountId disabledBy = StaffAccountId.generate();

            // When
            staffAccount.disable(disabledBy);

            // Then
            List<DomainEvent> enqueuedEvents = staffAccount.dequeueUncommittedEvents();
            assertThat(enqueuedEvents).isNotEmpty();

            StaffAccountDisabledEvent disabledEvent = (StaffAccountDisabledEvent) enqueuedEvents.getFirst();
            assertThatEventDataIsCorrect(disabledEvent, staffAccount);
        }

        @Test
        void shouldThrowStaffAccountException_whenAccountIsAlreadyDisabled() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withStatus(StaffAccountStatus.DISABLED)
                    .build();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> staffAccount.disable(StaffAccountId.generate()))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(StaffAccountExceptionInfo.accountAlreadyDisabled().getMessage(), StaffAccountExceptionInfo.accountAlreadyDisabled().getDomainErrorCode());
        }
    }

    @Nested
    class ResetPassword {
        @Test
        void shouldResetPassword() {
            // Given
            Instant oldPasswordIssuedAt = Instant.now().minus(1, ChronoUnit.DAYS);
            StaffAccountId resetBy = StaffAccountId.generate();
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withPassword("old-password")
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withPasswordTemporary(false)
                    .withPasswordIssuedAt(oldPasswordIssuedAt)
                    .withFailedLoginAttempts(3)
                    .build();

            // When
            staffAccount.resetPassword(Password.of("hashed-password"), resetBy);

            // Then
            assertThat(staffAccount.getPassword()).isEqualTo(Password.of("hashed-password"));
            assertThat(staffAccount.getPasswordResetBy()).isEqualTo(resetBy);
            assertThat(staffAccount.getStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
            assertThat(staffAccount.isPasswordTemporary()).isTrue();
            assertThat(staffAccount.getPasswordIssuedAt()).isAfter(oldPasswordIssuedAt);
            assertThat(staffAccount.getFailedLoginAttempts()).isEqualTo(FailedLoginAttempts.zero());
            assertThat(staffAccount.getVersion().getValue()).isEqualTo(2);
        }

        @Test
        void shouldEnqueueStaffAccountPasswordResetEvent() {
            // Given
            StaffAccountId resetBy = StaffAccountId.generate();
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();

            // When
            staffAccount.resetPassword(Password.of("hashed-password"), resetBy);

            //Then
            List<DomainEvent> enqueuedEvents = staffAccount.dequeueUncommittedEvents();
            assertThat(enqueuedEvents).isNotEmpty();

            StaffAccountPasswordResetEvent passwordResetEvent = (StaffAccountPasswordResetEvent) enqueuedEvents.getFirst();
            assertThatEventDataIsCorrect(passwordResetEvent, staffAccount);
        }

        @Test
        void shouldThrowStaffAccountException_whenAccountIsDisabled() {
            // Given
            StaffAccountId resetBy = StaffAccountId.generate();
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withStatus(StaffAccountStatus.DISABLED)
                    .build();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> staffAccount.resetPassword(Password.of("hashed-password"), resetBy))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(StaffAccountExceptionInfo.accountAlreadyDisabled().getMessage(), StaffAccountExceptionInfo.accountAlreadyDisabled().getDomainErrorCode());
        }
    }

    @Nested
    class Enable {
        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void shouldEnableStaffAccountAndRevertToCorrectStatus(boolean isPasswordTemporary) {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withDisabledBy(UUID.randomUUID().toString())
                    .withFailedLoginAttempts(3)
                    .withPasswordTemporary(isPasswordTemporary)
                    .withVersion(3)
                    .build();
            StaffAccountId enabledBy = StaffAccountId.generate();

            // When
            staffAccount.enable(enabledBy);

            // Then
            assertThat(staffAccount.getStatus()).isEqualTo(isPasswordTemporary ? StaffAccountStatus.PENDING_PASSWORD_CHANGE : StaffAccountStatus.ACTIVE);
            assertThat(staffAccount.getDisabledBy()).isNull();
            assertThat(staffAccount.getEnabledBy()).isEqualTo(enabledBy);
            assertThat(staffAccount.getFailedLoginAttempts()).isEqualTo(FailedLoginAttempts.zero());
            assertThat(staffAccount.getVersion()).isEqualTo(Version.of(4));
        }

        @Test
        void shouldEnqueueStaffAccountPasswordResetEvent() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withDisabledBy(UUID.randomUUID().toString())
                    .withFailedLoginAttempts(3)
                    .withPasswordTemporary(true)
                    .withVersion(3)
                    .build();
            StaffAccountId enabledBy = StaffAccountId.generate();

            // When
            staffAccount.enable(enabledBy);

            //Then
            List<DomainEvent> enqueuedEvents = staffAccount.dequeueUncommittedEvents();
            assertThat(enqueuedEvents).isNotEmpty();

            StaffAccountEnabledEvent enabledEvent = (StaffAccountEnabledEvent) enqueuedEvents.getFirst();
            assertThatEventDataIsCorrect(enabledEvent, staffAccount);
        }
    }

    @Nested
    class EnsureCanUpdatePassword {
        @Test
        void shouldNotThrow_whenStaffAccountIsInAValidState() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();

            // When & Then
            assertThatNoException().isThrownBy(staffAccount::ensureCanUpdatePassword);
        }

        @Test
        void shouldThrow_whenStaffAccountIsDisabled() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.disabledStaffAccount();
            StaffAccountException expectedException = new StaffAccountException(StaffAccountExceptionInfo.passwordChangeNotAllowedForDisabledAccount());

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(staffAccount::ensureCanUpdatePassword)
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
        }

        @Test
        void shouldThrow_whenStaffAccountIsLocked() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.lockedStaffAccount();
            StaffAccountException expectedException = new StaffAccountException(StaffAccountExceptionInfo.passwordChangeNotAllowedForLockedAccount());

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(staffAccount::ensureCanUpdatePassword)
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedException.getMessage(), expectedException.getDomainErrorCode());
        }

    }

    private static void assertThatEventDataIsCorrect(StaffAccountEventBase event, StaffAccount staffAccount) {
        assertThat(event.getStaffAccountId()).isEqualTo(staffAccount.getId());
        assertThat(event.getUsername()).isEqualTo(staffAccount.getUsername());
        assertThat(event.getEmail()).isEqualTo(staffAccount.getEmail());
        assertThat(event.getPassword()).isEqualTo(staffAccount.getPassword());
        assertThat(event.getPasswordIssuedAt()).isEqualTo(staffAccount.getPasswordIssuedAt());
        assertThat(event.getOrderAccessDuration()).isEqualTo(staffAccount.getOrderAccessDuration());
        assertThat(event.getModmailTranscriptAccessDuration()).isEqualTo(staffAccount.getModmailTranscriptAccessDuration());
        assertThat(event.getStaffAccountStatus()).isEqualTo(staffAccount.getStatus());
        assertThat(event.getFailedLoginAttempts()).isEqualTo(staffAccount.getFailedLoginAttempts());
        assertThat(event.getLockedUntil()).isEqualTo(staffAccount.getLockedUntil());
        assertThat(event.getLastLoginAt()).isEqualTo(staffAccount.getLastLoginAt());
        assertThat(event.getStaffAccountCreatedBy()).isEqualTo(staffAccount.getCreatedBy());
        assertThat(event.getStaffAccountDisabledBy()).isEqualTo(staffAccount.getDisabledBy());
        assertThat(event.getStaffAccountEnabledBy()).isEqualTo(staffAccount.getEnabledBy());
        assertThat(event.getStaffAccountPasswordResetBy()).isEqualTo(staffAccount.getPasswordResetBy());
        assertThat(event.getPermissionCodes()).isEqualTo(staffAccount.getPermissionCodes());
        assertThat(event.getStaffAccountVersion()).isEqualTo(staffAccount.getVersion());
    }
}
