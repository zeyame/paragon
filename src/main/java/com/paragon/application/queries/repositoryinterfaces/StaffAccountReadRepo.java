package com.paragon.application.queries.repositoryinterfaces;

import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StaffAccountReadRepo {
    boolean exists(StaffAccountId staffAccountId);
    boolean hasPermission(StaffAccountId staffAccountId, PermissionCode permissionCode);
    List<StaffAccountSummaryReadModel> findAll();
    List<StaffAccountSummaryReadModel> findAll(String status,
                                               StaffAccountId enabledBy,
                                               StaffAccountId disabledBy,
                                               Instant createdBefore,
                                               Instant createdAfter);
    Optional<StaffAccountSummaryReadModel> findByUsername(Username username);
}
