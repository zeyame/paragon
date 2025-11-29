package com.paragon.application.common.exceptions;

import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenException;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.aggregate.RefreshTokenExceptionInfo;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.exceptions.entity.AuditTrailEntryException;
import com.paragon.domain.exceptions.entity.AuditTrailEntryExceptionInfo;
import com.paragon.domain.exceptions.entity.PermissionException;
import com.paragon.domain.exceptions.entity.PermissionExceptionInfo;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyException;
import com.paragon.domain.exceptions.services.StaffAccountPasswordReusePolicyExceptionInfo;
import com.paragon.domain.exceptions.valueobject.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class AppExceptionHandlerTests {
    @Nested
    class HandleDomainException {
        private final AppExceptionHandlerImpl sut;

        public HandleDomainException() {
            this.sut = new AppExceptionHandlerImpl();
        }

        @ParameterizedTest
        @MethodSource("provideDomainExceptions")
        void shouldMapToCorrectAppException(DomainException domainException, AppExceptionStatusCode expectedStatusCode) {
            // When
            AppException translatedAppException = sut.handleDomainException(domainException);

            // Then
            assertThat(translatedAppException.getMessage()).isEqualTo(domainException.getMessage());
            assertThat(translatedAppException.getErrorCode()).isEqualTo(domainException.getDomainErrorCode());
            assertThat(translatedAppException.getStatusCode()).isEqualTo(expectedStatusCode);
        }

        private static Stream<Arguments> provideDomainExceptions() {
            return Stream.of(
                    // StaffAccountException - validation errors (CLIENT_ERROR)
                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.usernameRequired()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.passwordRequired()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.orderAccessDurationRequired()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.modmailTranscriptAccessDurationRequired()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.createdByRequired()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.atLeastOnePermissionRequired()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // StaffAccountException - AUTH Failures
                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.loginFailedAccountDisabled()),
                            AppExceptionStatusCode.AUTHENTICATION_FAILED
                    ),

                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.loginFailedAccountLocked()),
                            AppExceptionStatusCode.AUTHENTICATION_FAILED
                    ),

                    // StaffAccountException - invalid credentials (AUTHENTICATION_FAILED)
                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.invalidCredentials()),
                            AppExceptionStatusCode.AUTHENTICATION_FAILED
                    ),

                    // StaffAccountException - state conflict (INVALID_RESOURCE_STATE)
                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.accountAlreadyDisabled()),
                            AppExceptionStatusCode.INVALID_RESOURCE_STATE
                    ),

                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.accountAlreadyEnabled()),
                            AppExceptionStatusCode.INVALID_RESOURCE_STATE
                    ),

                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.passwordChangeRequiresActiveAccount()),
                            AppExceptionStatusCode.INVALID_RESOURCE_STATE
                    ),

                    arguments(
                            new StaffAccountException(StaffAccountExceptionInfo.temporaryPasswordChangeRequiresPendingState()),
                            AppExceptionStatusCode.INVALID_RESOURCE_STATE
                    ),

                    // AuditTrailEntryException - internal errors (SERVER_ERROR)
                    arguments(
                            new AuditTrailEntryException(AuditTrailEntryExceptionInfo.actorIdRequired()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new AuditTrailEntryException(AuditTrailEntryExceptionInfo.actionTypeRequired()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // PermissionException - internal errors (SERVER_ERROR)
                    arguments(
                            new PermissionException(PermissionExceptionInfo.codeRequired()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new PermissionException(PermissionExceptionInfo.categoryRequired()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // VersionException - internal error (SERVER_ERROR)
                    arguments(
                            new VersionException(VersionExceptionInfo.mustBeAtleastOne()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // EventIdException - internal error (SERVER_ERROR)
                    arguments(
                            new EventIdException(EventIdExceptionInfo.mustNotBeNull()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // StaffAccountIdException - internal error (SERVER_ERROR)
                    arguments(
                            new StaffAccountIdException(StaffAccountIdExceptionInfo.missingValue()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new StaffAccountIdException(StaffAccountIdExceptionInfo.invalidFormat()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // UsernameException - user input (CLIENT_ERROR)
                    arguments(
                            new UsernameException(UsernameExceptionInfo.missingValue()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new UsernameException(UsernameExceptionInfo.lengthOutOfRange()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new UsernameException(UsernameExceptionInfo.invalidCharacters()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new UsernameException(UsernameExceptionInfo.consecutiveUnderscores()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new UsernameException(UsernameExceptionInfo.mustStartWithALetter()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new UsernameException(UsernameExceptionInfo.mustNotEndWithUnderscore()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new UsernameException(UsernameExceptionInfo.reservedWord()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // EmailException - user input (CLIENT_ERROR)
                    arguments(
                            new EmailException(EmailExceptionInfo.missingValue()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new EmailException(EmailExceptionInfo.lengthOutOfRange()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new EmailException(EmailExceptionInfo.invalidFormat()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // DateTimeUtcException - user input (CLIENT_ERROR)
                    arguments(
                            new DateTimeUtcException(DateTimeUtcExceptionInfo.missingValue()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new DateTimeUtcException(DateTimeUtcExceptionInfo.invalidFormat()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // PasswordException - internal error (SERVER_ERROR)
                    arguments(
                            new PasswordException(PasswordExceptionInfo.missingValue()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // PasswordHistoryEntryException - internal errors (SERVER_ERROR)
                    arguments(
                            new PasswordHistoryEntryException(PasswordHistoryEntryExceptionInfo.missingStaffAccountId()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new PasswordHistoryEntryException(PasswordHistoryEntryExceptionInfo.missingHashedPassword()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new PasswordHistoryEntryException(PasswordHistoryEntryExceptionInfo.missingChangedAtTimestamp()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // StaffAccountPasswordHistoryException - internal errors (SERVER_ERROR)
                    arguments(
                            new StaffAccountPasswordHistoryException(StaffAccountPasswordHistoryExceptionInfo.mustContainEntries()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new StaffAccountPasswordHistoryException(StaffAccountPasswordHistoryExceptionInfo.entriesMustBelongToSingleAccount()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // StaffAccountPasswordReusePolicyException - business rule (CLIENT_ERROR)
                    arguments(
                            new StaffAccountPasswordReusePolicyException(StaffAccountPasswordReusePolicyExceptionInfo.passwordUsedWithinRestrictedWindow()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // PlaintextPasswordException - user input (CLIENT_ERROR)
                    arguments(
                            new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingValue()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PlaintextPasswordException(PlaintextPasswordExceptionInfo.tooShort(8)),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PlaintextPasswordException(PlaintextPasswordExceptionInfo.tooLong(128)),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingUppercase()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingLowercase()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingDigit()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingSpecialCharacter()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PlaintextPasswordException(PlaintextPasswordExceptionInfo.containsWhitespace()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // PlaintextRefreshTokenException - internal error (SERVER_ERROR)
                    arguments(
                            new PlaintextRefreshTokenException(PlaintextRefreshTokenExceptionInfo.missingValue()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // RefreshTokenException - internal errors and invalid state
                    arguments(
                            new RefreshTokenException(RefreshTokenExceptionInfo.tokenHashRequired()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new RefreshTokenException(RefreshTokenExceptionInfo.staffAccountIdRequired()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new RefreshTokenException(RefreshTokenExceptionInfo.ipAddressRequired()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new RefreshTokenException(RefreshTokenExceptionInfo.tokenAlreadyRevoked()),
                            AppExceptionStatusCode.INVALID_RESOURCE_STATE
                    ),

                    // OrderAccessDurationException - user input (CLIENT_ERROR)
                    arguments(
                            new OrderAccessDurationException(OrderAccessDurationExceptionInfo.mustBePositive()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // ModmailTranscriptAccessDurationException - user input (CLIENT_ERROR)
                    arguments(
                            new ModmailTranscriptAccessDurationException(ModmailTranscriptAccessDurationExceptionInfo.mustBePositive()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // FailedLoginAttemptsException - internal error (SERVER_ERROR)
                    arguments(
                            new FailedLoginAttemptsException(FailedLoginAttemptsExceptionInfo.invalidAttemptNumber()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // FailedLoginAttemptsException - max attempts (INVALID_RESOURCE_STATE)
                    arguments(
                            new FailedLoginAttemptsException(FailedLoginAttemptsExceptionInfo.maxAttemptsReached()),
                            AppExceptionStatusCode.INVALID_RESOURCE_STATE
                    ),

                    // PermissionIdException - internal error (SERVER_ERROR)
                    arguments(
                            new PermissionIdException(PermissionIdExceptionInfo.missingValue()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new PermissionIdException(PermissionIdExceptionInfo.invalidFormat()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // PermissionCodeException - user input (CLIENT_ERROR)
                    arguments(
                            new PermissionCodeException(PermissionCodeExceptionInfo.missingValue()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PermissionCodeException(PermissionCodeExceptionInfo.lengthOutOfRange()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PermissionCodeException(PermissionCodeExceptionInfo.invalidCharacters()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PermissionCodeException(PermissionCodeExceptionInfo.mustNotStartOrEndWithUnderscore()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),
                    arguments(
                            new PermissionCodeException(PermissionCodeExceptionInfo.consecutiveUnderscores()),
                            AppExceptionStatusCode.CLIENT_ERROR
                    ),

                    // AuditEntryIdException - internal error (SERVER_ERROR)
                    arguments(
                            new AuditEntryIdException(AuditEntryIdExceptionInfo.missingValue()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),
                    arguments(
                            new AuditEntryIdException(AuditEntryIdExceptionInfo.invalidFormat()),
                            AppExceptionStatusCode.SERVER_ERROR
                    ),

                    // AuditEntryTargetIdException - internal error (SERVER_ERROR)
                    arguments(
                            new AuditEntryTargetIdException(AuditEntryTargetIdExceptionInfo.missingValue()),
                            AppExceptionStatusCode.SERVER_ERROR
                    )
            );
        }
    }
}
