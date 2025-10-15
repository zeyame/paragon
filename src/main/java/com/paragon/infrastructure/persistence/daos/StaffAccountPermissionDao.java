package com.paragon.infrastructure.persistence.daos;

import java.time.Instant;
import java.util.UUID;

public record StaffAccountPermissionDao(
        UUID staffAccountId,
        String permissionCode,
        UUID assignedBy,
        Instant assignedAtUtc
) {}