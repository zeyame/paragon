package com.paragon.application.queries.repositoryinterfaces;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.valueobjects.DateTimeUtc;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountDetailedReadModel;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffAccountReadRepo {
    boolean exists(UUID staffAccountId);
    boolean hasPermission(UUID staffAccountId, PermissionCode permissionCode);
    List<StaffAccountSummaryReadModel> findAllSummaries(StaffAccountStatus status,
                                                        Username enabledBy,
                                                        Username disabledBy,
                                                        DateTimeUtc createdBefore,
                                                        DateTimeUtc createdAfter);
    Optional<StaffAccountSummaryReadModel> findSummaryByUsername(String username);
    Optional<StaffAccountDetailedReadModel> findDetailedById(UUID staffAccountId);
    Optional<StaffAccountStatus> findStatusById(UUID staffAccountId);
}
