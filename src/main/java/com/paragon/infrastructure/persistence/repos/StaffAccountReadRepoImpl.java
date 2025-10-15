package com.paragon.infrastructure.persistence.repos;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.daos.StaffAccountPermissionDao;
import com.paragon.infrastructure.persistence.jdbc.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class StaffAccountReadRepoImpl implements StaffAccountReadRepo {
    private final ReadJdbcHelper readJdbcHelper;

    public StaffAccountReadRepoImpl(ReadJdbcHelper readJdbcHelper) {
        this.readJdbcHelper = readJdbcHelper;
    }

    @Override
    public boolean exists(StaffAccountId staffAccountId) {
        String sql = "SELECT id FROM staff_accounts WHERE id = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", staffAccountId.getValue());

        return readJdbcHelper.queryFirstOrDefault(sql, params, UUID.class).isPresent();
    }

    @Override
    public boolean hasPermission(StaffAccountId staffAccountId, PermissionCode permissionCode) {
        String sql = "SELECT * FROM staff_account_permissions WHERE staff_account_id = :staffAccountId AND permission_code = :permissionCode";
        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("staffAccountId", staffAccountId.getValue())
                .add("permissionCode", permissionCode.getValue());

        return readJdbcHelper.queryFirstOrDefault(sql, params, StaffAccountPermissionDao.class).isPresent();
    }

    @Override
    public List<StaffAccountSummaryReadModel> findAllSummaries() {
        return List.of();
    }
}
