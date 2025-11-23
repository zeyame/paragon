package com.paragon.application.queries.getallstaffaccounts;

import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;

import java.time.Instant;
import java.util.UUID;

public record StaffAccountSummary(
        UUID id,
        String username,
        String status,
        Instant createdAtUtc
) {
    public static StaffAccountSummary fromReadModel(StaffAccountSummaryReadModel rm) {
        return new StaffAccountSummary(
                rm.id(),
                rm.username(),
                rm.status(),
                rm.createdAtUtc()
        );
    }
}
