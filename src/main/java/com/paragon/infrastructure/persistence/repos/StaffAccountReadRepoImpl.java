package com.paragon.infrastructure.persistence.repos;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaffAccountReadRepoImpl implements StaffAccountReadRepo {
    @Override
    public boolean exists(StaffAccountId staffAccountId) {
        return false;
    }

    @Override
    public boolean hasPermission(StaffAccountId staffAccountId, PermissionCode permissionCode) {
        return false;
    }

    @Override
    public List<StaffAccountSummaryReadModel> findAllSummaries() {
        return List.of();
    }
}
