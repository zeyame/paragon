package com.paragon.application.queries.repositoryinterfaces;

import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;

import java.util.List;

public interface StaffAccountReadRepo {
    boolean exists(StaffAccountId staffAccountId);
    boolean hasPermission(StaffAccountId staffAccountId, PermissionCode permissionCode);
    List<StaffAccountSummaryReadModel> findAllSummaries();
}
