package com.paragon.infrastructure.persistence.repos;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.jdbc.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaffAccountReadRepoImpl implements StaffAccountReadRepo {
    private final ReadJdbcHelper readJdbcHelper;

    public StaffAccountReadRepoImpl(ReadJdbcHelper readJdbcHelper) {
        this.readJdbcHelper = readJdbcHelper;
    }

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
