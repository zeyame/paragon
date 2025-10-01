package com.paragon.domain.models.aggregates;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.models.valueobjects.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

public class StaffAccountTests {
    @Nested
    class Register {
        private Username username;
        private Email email;
        private Password password;
        private OrderAccessDuration orderAccessDuration;
        private ModmailTranscriptAccessDuration modmailTranscriptAccessDuration;
        private StaffAccountId createdBy;
        private Set<PermissionId> permissionIds;

        Register() {
            username = Username.of("john_doe");
            email = Email.of("john_doe@example.com");
            password = Password.of("$argon2id$v=19$m=65536,t=3,p=1$QWxhZGRpbjpPcGVuU2VzYW1l$2iYvT1yzFzHtXJH7zM4jW1Z2sK7Tg==");
            orderAccessDuration = OrderAccessDuration.from(5);
            modmailTranscriptAccessDuration = ModmailTranscriptAccessDuration.from(10);
            createdBy = StaffAccountId.generate();
            permissionIds = Set.of(PermissionId.generate(), PermissionId.generate());
        }

        @Test
        void givenValidInputWithoutEmail_shouldCreateStaffAccount() {
            // When
            StaffAccount staffAccount = StaffAccount.register(username, null, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionIds);

            // Then
            assertThat(staffAccount).isNotNull();
            assertThat(staffAccount.getUsername()).isEqualTo(username);
            assertThat(staffAccount.getEmail()).isNull();
            assertThat(staffAccount.getPassword()).isEqualTo(password);
            assertThat(staffAccount.getPasswordIssuedAt()).isNotNull();
            assertThat(staffAccount.getOrderAccessDuration()).isEqualTo(orderAccessDuration);
            assertThat(staffAccount.getModmailTranscriptAccessDuration()).isEqualTo(modmailTranscriptAccessDuration);
            assertThat(staffAccount.getStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
            assertThat(staffAccount.getFailedLoginAttempts().getValue()).isZero();
            assertThat(staffAccount.getLockedUntil()).isNull();
            assertThat(staffAccount.getLastLoginAt()).isNull();
            assertThat(staffAccount.getCreatedBy()).isEqualTo(createdBy);
            assertThat(staffAccount.getPermissionIds()).isEqualTo(permissionIds);
        }

        @Test
        void givenValidInputWithEmail_shouldCreateStaffAccount() {
            // When
            StaffAccount staffAccount = StaffAccount.register(username, email, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionIds);

            // Then
            assertThat(staffAccount).isNotNull();
            assertThat(staffAccount.getUsername()).isEqualTo(username);
            assertThat(staffAccount.getEmail()).isEqualTo(email);
            assertThat(staffAccount.getPassword()).isEqualTo(password);
            assertThat(staffAccount.getPasswordIssuedAt()).isNotNull();
            assertThat(staffAccount.getOrderAccessDuration()).isEqualTo(orderAccessDuration);
            assertThat(staffAccount.getModmailTranscriptAccessDuration()).isEqualTo(modmailTranscriptAccessDuration);
            assertThat(staffAccount.getStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
            assertThat(staffAccount.getFailedLoginAttempts().getValue()).isZero();
            assertThat(staffAccount.getLockedUntil()).isNull();
            assertThat(staffAccount.getLastLoginAt()).isNull();
            assertThat(staffAccount.getCreatedBy()).isEqualTo(createdBy);
            assertThat(staffAccount.getPermissionIds()).isEqualTo(permissionIds);
        }

        @Test
        void givenMissingUsername_registrationShouldFail() {
            // Given
            String expectedErrorMessage = StaffAccountExceptionInfo.usernameRequired().getMessage();
            int expectedErrorCode = StaffAccountExceptionInfo.usernameRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> StaffAccount.register(null, email, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionIds))
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
                    .isThrownBy(() -> StaffAccount.register(username, email, null, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, permissionIds))
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
                    .isThrownBy(() -> StaffAccount.register(username, email, password, null, modmailTranscriptAccessDuration, createdBy, permissionIds))
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
                    .isThrownBy(() -> StaffAccount.register(username, email, password, orderAccessDuration, null, createdBy, permissionIds))
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
                    .isThrownBy(() -> StaffAccount.register(username, email, password, orderAccessDuration, modmailTranscriptAccessDuration, null, permissionIds))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }

        @Test
        void givenNoPermissionAssigned_registrationShouldFail() {
            // Given
            String expectedErrorMessage = StaffAccountExceptionInfo.atLeastOnePermissionRequired().getMessage();
            int expectedErrorCode = StaffAccountExceptionInfo.atLeastOnePermissionRequired().getDomainErrorCode();

            // When & Then
            assertThatExceptionOfType(StaffAccountException.class)
                    .isThrownBy(() -> StaffAccount.register(username, email, password, orderAccessDuration, modmailTranscriptAccessDuration, createdBy, new HashSet<>()))
                    .extracting("message", "domainErrorCode")
                    .containsExactly(expectedErrorMessage, expectedErrorCode);
        }
    }
}
