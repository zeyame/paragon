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
        Instant passwordIssuedAt,
        int orderAccessDuration,
        int modmailTranscriptAccessDuration,
        String status,
        int failedLoginAttempts,
        Instant lockedUntil,
        Instant lastLoginAt,
        UUID createdBy,
        UUID disabledBy,
        List<UUID> permissionIds,
        int version,
        Instant createdAtUtc,
        Instant updatedAtUtc
) {

    public StaffAccount toStaffAccount(List<PermissionId> permissionIds) {
        return StaffAccount.createFrom(
                StaffAccountId.of(id),
                Username.of(username),
                Email.of(email),
                Password.of(password),
                passwordIssuedAt,
                OrderAccessDuration.from(orderAccessDuration),
                ModmailTranscriptAccessDuration.from(modmailTranscriptAccessDuration),
                StaffAccountStatus.valueOf(status),
                FailedLoginAttempts.of(failedLoginAttempts),
                lockedUntil,
                lastLoginAt,
                StaffAccountId.of(createdBy),
                disabledBy != null ? StaffAccountId.of(disabledBy) : null,
                new HashSet<>(permissionIds),
                Version.of(version)
        );
    }
}
