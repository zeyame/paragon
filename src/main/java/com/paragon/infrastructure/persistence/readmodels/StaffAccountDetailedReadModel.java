package com.paragon.infrastructure.persistence.readmodels;

import com.paragon.infrastructure.persistence.daos.StaffAccountDetailedReadModelDao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StaffAccountDetailedReadModel(
        UUID id,
        String username,
        int orderAccessDurationInDays,
        int modmailTranscriptAccessDurationInDays,
        String status,
        Instant lockedUntilUtc,
        Instant lastLoginAtUtc,
        UUID createdBy,
        UUID disabledBy,
        List<String> permissionCodes,
        Instant createdAtUtc
) {
    public static StaffAccountDetailedReadModel from(
            StaffAccountDetailedReadModelDao dao,
            List<String> permissionCodes
    ) {
        return new StaffAccountDetailedReadModel(
                dao.id(),
                dao.username(),
                dao.orderAccessDurationInDays(),
                dao.modmailTranscriptAccessDurationInDays(),
                dao.status(),
                dao.lockedUntilUtc(),
                dao.lastLoginAtUtc(),
                dao.createdBy(),
                dao.disabledBy(),
                permissionCodes,
                dao.createdAtUtc()
        );
    }
}
