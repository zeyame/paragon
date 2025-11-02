package com.paragon.domain.models.aggregates;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.DomainEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountLoggedInEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountRegisteredEvent;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.interfaces.PasswordHasher;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

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
            assertThat(event.getPermissionCodes()).isEqualTo(staffAccount.getPermissionCodes());
            assertThat(event.getStaffAccountVersion()).isEqualTo(staffAccount.getVersion());
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
    class Login {
        private final PasswordHasher passwordHasherMock;

        public Login() {
            passwordHasherMock = mock(PasswordHasher.class);
        }

        @Test
        void shouldLoginSuccessfully() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();
            String enteredPassword = "Password123?";
            when(passwordHasherMock.verify(anyString(), anyString()))
                    .thenReturn(true);

            // When
            LoginResult result = staffAccount.login(enteredPassword, passwordHasherMock);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.failureReason()).isNull();
            assertThat(staffAccount.getFailedLoginAttempts().getValue()).isZero();
            assertThat(staffAccount.getLastLoginAt()).isNotNull();
            assertThat(staffAccount.getLastLoginAt()).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        void shouldResetFailedLoginAttempts_uponSuccessfulLogin() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withFailedLoginAttempts(3)
                    .build();
            String enteredPassword = "Password123?";
            when(passwordHasherMock.verify(anyString(), anyString()))
                    .thenReturn(true);

            // When
            staffAccount.login(enteredPassword, passwordHasherMock);

            // Then
            assertThat(staffAccount.getFailedLoginAttempts().getValue()).isZero();
        }

        @Test
        void shouldIncreaseVersion_uponSuccessfulLogin() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();
            String enteredPassword = "Password123?";
            when(passwordHasherMock.verify(anyString(), anyString()))
                    .thenReturn(true);

            // When
            staffAccount.login(enteredPassword, passwordHasherMock);

            // Then
            assertThat(staffAccount.getVersion().getValue()).isEqualTo(2);
        }

        @Test
        void shouldEnqueueStaffAccountLoggedInEvent_uponSuccessfulLogin() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();
            String enteredPassword = "Password123?";
            when(passwordHasherMock.verify(anyString(), anyString()))
                    .thenReturn(true);

            // When
            staffAccount.login(enteredPassword, passwordHasherMock);

            // Then
            List<DomainEvent> queuedEvents = staffAccount.dequeueUncommittedEvents();
            assertThat(queuedEvents).hasSize(1);
            assertThat(queuedEvents.getFirst()).isInstanceOf(StaffAccountLoggedInEvent.class);

            StaffAccountLoggedInEvent event = (StaffAccountLoggedInEvent) queuedEvents.getFirst();
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
            assertThat(event.getPermissionCodes()).isEqualTo(staffAccount.getPermissionCodes());
            assertThat(event.getStaffAccountVersion()).isEqualTo(staffAccount.getVersion());
        }

        @Test
        void shouldThrowStaffAccountException_whenAccountIsDisabledAndPasswordIsCorrect() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withStatus(StaffAccountStatus.DISABLED)
                    .build();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> staffAccount.login("Correctpassword123?", passwordHasherMock))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(StaffAccountExceptionInfo.loginFailedAccountDisabled().getMessage(), StaffAccountExceptionInfo.loginFailedAccountDisabled().getDomainErrorCode());
        }

        @Test
        void shouldThrowStaffAccountException_whenAccountIsLockedAndPasswordIsCorrect() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withStatus(StaffAccountStatus.LOCKED)
                    .withLockedUntil(Instant.now().plus(Duration.ofMinutes(15)))
                    .build();

            String hashedPasswordValue = staffAccount.getPassword().getValue();
            Password enteredPassword = Password.fromHashed(hashedPasswordValue);

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> staffAccount.login("Correctpassword123?", passwordHasherMock))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(StaffAccountExceptionInfo.loginFailedAccountLocked().getMessage(), StaffAccountExceptionInfo.loginFailedAccountLocked().getDomainErrorCode());
        }

        @Test
        void shouldReturnFailedLoginResult_whenEnteredPasswordIsIncorrect() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();
            String enteredPassword = "Incorrectpassword123?";

            when(passwordHasherMock.verify(anyString(), anyString()))
                    .thenReturn(false);

            // When
            LoginResult result = staffAccount.login(enteredPassword, passwordHasherMock);

            // Then
            assertThat(result.isFailed()).isTrue();
            assertThat(result.failureReason()).isEqualTo("Invalid credentials");
        }

        @Test
        void shouldIncrementFailedLoginAttempts_whenEnteredPasswordIsIncorrect() {
            // Given
            StaffAccount staffAccount = StaffAccountFixture.validStaffAccount();
            String enteredPassword = "Incorrectpassword123?";

            when(passwordHasherMock.verify(anyString(), anyString()))
                    .thenReturn(false);

            // When
            LoginResult result = staffAccount.login(enteredPassword, passwordHasherMock);

            // Then
            assertThat(result.isFailed()).isTrue();
            assertThat(staffAccount.getFailedLoginAttempts().getValue()).isEqualTo(1);
        }

        @Test
        void locksAccountForFifteenMinutes_whenMaximumFailedLoginAttemptsAreReached() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withFailedLoginAttempts(4)
                    .build();
            String enteredPassword = "Incorrectpassword123?";

            when(passwordHasherMock.verify(anyString(), anyString()))
                    .thenReturn(false);

            // When
            LoginResult result = staffAccount.login(enteredPassword, passwordHasherMock);

            // Then
            assertThat(result.isFailed()).isTrue();
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
            String enteredPassword = "Incorrectpassword123?";

            when(passwordHasherMock.verify(anyString(), anyString()))
                    .thenReturn(false);

            // When
            LoginResult result = staffAccount.login(enteredPassword, passwordHasherMock);

            // Then
            assertThat(result.isFailed()).isTrue();

            List<DomainEvent> enqueuedEvents = staffAccount.dequeueUncommittedEvents();
            assertThat(enqueuedEvents).hasSize(1);
            assertThat(enqueuedEvents.getFirst()).isInstanceOf(StaffAccountLockedEvent.class);

            StaffAccountLockedEvent event = (StaffAccountLockedEvent) enqueuedEvents.getFirst();
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
            assertThat(event.getPermissionCodes()).isEqualTo(staffAccount.getPermissionCodes());
            assertThat(event.getStaffAccountVersion()).isEqualTo(staffAccount.getVersion());
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
            String enteredPassword = "Password123?";
            when(passwordHasherMock.verify(anyString(), anyString()))
                    .thenReturn(true);

            // When
            staffAccount.login(enteredPassword, passwordHasherMock);

            // Then
            assertThat(staffAccount.getStatus()).isEqualTo(isPasswordTemporary ? StaffAccountStatus.PENDING_PASSWORD_CHANGE : StaffAccountStatus.ACTIVE);
            assertThat(staffAccount.getLockedUntil()).isNull();
            assertThat(staffAccount.getLastLoginAt()).isNotNull();
        }
    }
}
