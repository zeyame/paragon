package com.paragon.application.common.exceptions;

import com.paragon.application.common.interfaces.AppExceptionHandler;
import com.paragon.domain.exceptions.DomainException;
import com.paragon.domain.exceptions.aggregate.StaffAccountException;
import com.paragon.domain.exceptions.entity.AuditTrailEntryException;
import com.paragon.domain.exceptions.entity.PermissionException;
import com.paragon.domain.exceptions.valueobject.*;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.springframework.stereotype.Component;

@Component
public class AppExceptionHandlerImpl implements AppExceptionHandler {
    @Override
    public AppException handleDomainException(DomainException domainException) {
        return switch (domainException) {
            case StaffAccountException staffAccountException -> handleStaffAccountException(staffAccountException);
            case AuditTrailEntryException auditTrailEntryException -> handleAuditTrailEntryException(auditTrailEntryException);
            case PermissionException permissionException -> handlePermissionException(permissionException);

            // value object exceptions
            case VersionException versionException -> handleVersionException(versionException);
            case EventIdException eventIdException -> handleEventIdException(eventIdException);
            case StaffAccountIdException staffAccountIdException -> handleStaffAccountIdException(staffAccountIdException);
            case UsernameException usernameException -> handleUsernameException(usernameException);
            case EmailException emailException -> handleEmailException(emailException);
            case PasswordException passwordException -> handlePasswordException(passwordException);
            case PlaintextPasswordException plaintextPasswordException -> handlePlaintextPasswordException(plaintextPasswordException);
            case OrderAccessDurationException orderAccessDurationException -> handleOrderAccessDurationException(orderAccessDurationException);
            case ModmailTranscriptAccessDurationException modmailTranscriptAccessDurationException -> handleModmailTranscriptAccessDurationException(modmailTranscriptAccessDurationException);
            case FailedLoginAttemptsException failedLoginAttemptsException -> handleFailedLoginAttemptsException(failedLoginAttemptsException);
            case PermissionIdException permissionIdException -> handlePermissionIdException(permissionIdException);
            case PermissionCodeException permissionCodeException -> handlePermissionCodeException(permissionCodeException);
            case AuditEntryIdException auditEntryIdException -> handleAuditEntryIdException(auditEntryIdException);
            case AuditEntryTargetIdException auditEntryTargetIdException -> handleAuditEntryTargetIdException(auditEntryTargetIdException);

            default -> new AppException(domainException, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    @Override
    public AppException handleInfraException(InfraException infraException) {
        // TODO: Implement proper InfraException handling with InfraExceptionInfo
        return null;
    }

    private AppException handleStaffAccountException(StaffAccountException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 10001, 10002, 10003, 10004, 10005, 10006 -> // validation errors (required fields, at least one permission)
                    new AppException(exception, AppExceptionStatusCode.CLIENT_ERROR);

            case 10007, 10008, 10009 -> // authentication failures (account disabled/locked, invalid credentials)
                    new AppException(exception, AppExceptionStatusCode.AUTHENTICATION_FAILED);

            case 10010 -> // state conflict (account already disabled)
                    new AppException(exception, AppExceptionStatusCode.INVALID_RESOURCE_STATE);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleAuditTrailEntryException(AuditTrailEntryException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 201001, 201002, 201003 -> // validation errors (actorId, actionType, outcome required)
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handlePermissionException(PermissionException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 200001, 200002 -> // validation errors (code, category required) - internal entity creation
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleVersionException(VersionException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 100001 -> // version must be at least 1 - internal error
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleEventIdException(EventIdException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 101001 -> // event id cannot be null - internal error
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleStaffAccountIdException(StaffAccountIdException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 102001, 102002 -> // missing value, invalid format - internal error
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleUsernameException(UsernameException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 103001, 103002, 103003, 103004, 103005, 103006, 103007 -> // all username validation errors - user input
                    new AppException(exception, AppExceptionStatusCode.CLIENT_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleEmailException(EmailException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 104001, 104002, 104003 -> // missing value, length out of range, invalid format - user input
                    new AppException(exception, AppExceptionStatusCode.CLIENT_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handlePasswordException(PasswordException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 105001 -> // missing value - internal error (hashed password from DB)
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handlePlaintextPasswordException(PlaintextPasswordException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 113001, 113002, 113003, 113004, 113005, 113006, 113007, 113008 -> // all plaintext password validation errors - user input
                    new AppException(exception, AppExceptionStatusCode.CLIENT_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleOrderAccessDurationException(OrderAccessDurationException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 106001 -> // must be positive - user input (admin)
                    new AppException(exception, AppExceptionStatusCode.CLIENT_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleModmailTranscriptAccessDurationException(ModmailTranscriptAccessDurationException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 107001 -> // must be positive - user input (admin)
                    new AppException(exception, AppExceptionStatusCode.CLIENT_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleFailedLoginAttemptsException(FailedLoginAttemptsException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 108001 -> // invalid attempt number - internal error
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            case 108002 -> // max attempts reached - resource state issue
                    new AppException(exception, AppExceptionStatusCode.INVALID_RESOURCE_STATE);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handlePermissionIdException(PermissionIdException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 109001, 109002 -> // missing value, invalid format - internal error
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handlePermissionCodeException(PermissionCodeException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 110001, 110002, 110003, 110004, 110005 -> // all permission code validation errors - user input (admin)
                    new AppException(exception, AppExceptionStatusCode.CLIENT_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleAuditEntryIdException(AuditEntryIdException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 111001, 111002 -> // missing value, invalid format - internal error
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }

    private AppException handleAuditEntryTargetIdException(AuditEntryTargetIdException exception) {
        int domainErrorCode = exception.getDomainErrorCode();

        return switch (domainErrorCode) {
            case 112001 -> // missing value - internal error
                    new AppException(exception, AppExceptionStatusCode.SERVER_ERROR);

            default -> new AppException(exception, AppExceptionStatusCode.UNHANDLED_ERROR);
        };
    }
}
