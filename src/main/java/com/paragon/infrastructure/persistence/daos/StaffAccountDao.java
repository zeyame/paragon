package com.paragon.infrastructure.persistence.daos;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public record StaffAccountDao(
        UUID id,
        String username,
        String email,
        String password,
        Instant passwordIssuedAtUtc,
        int orderAccessDuration,
        int modmailTranscriptAccessDuration,
        String status,
        int failedLoginAttempts,
        Instant lockedUntilUtc,
        Instant lastLoginAtUtc,
        UUID createdBy,
        UUID disabledBy,
        int version,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {

    public StaffAccount toStaffAccount(List<PermissionCode> permissionCodes) {
        return StaffAccount.createFrom(
                StaffAccountId.of(id),
                Username.of(username),
                email != null ? Email.of(email) : null,
                Password.of(password),
                passwordIssuedAtUtc,
                OrderAccessDuration.from(orderAccessDuration),
                ModmailTranscriptAccessDuration.from(modmailTranscriptAccessDuration),
                StaffAccountStatus.valueOf(status),
                FailedLoginAttempts.of(failedLoginAttempts),
                lockedUntilUtc,
                lastLoginAtUtc,
                StaffAccountId.of(createdBy),
                disabledBy != null ? StaffAccountId.of(disabledBy) : null,
                new HashSet<>(permissionCodes),
                Version.of(version)
        );
    }
}
