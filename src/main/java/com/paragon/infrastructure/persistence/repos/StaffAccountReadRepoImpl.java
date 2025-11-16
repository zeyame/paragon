package com.paragon.infrastructure.persistence.repos;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.daos.StaffAccountIdDao;
import com.paragon.infrastructure.persistence.daos.StaffAccountPermissionDao;
import com.paragon.infrastructure.persistence.jdbc.helpers.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

        return readJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                StaffAccountIdDao.class
        ).isPresent();
    }

    @Override
    public boolean hasPermission(StaffAccountId staffAccountId, PermissionCode permissionCode) {
        String sql = "SELECT * FROM staff_account_permissions WHERE staff_account_id = :staffAccountId AND permission_code = :permissionCode";
        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("staffAccountId", staffAccountId.getValue())
                .add("permissionCode", permissionCode.getValue());

        return readJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                StaffAccountPermissionDao.class
        ).isPresent();
    }

    @Override
    public List<StaffAccountSummaryReadModel> findAll() {
        String sql = "SELECT id, username, status, order_access_duration, modmail_transcript_access_duration, created_at_utc FROM staff_accounts ORDER BY created_at_utc DESC";
        return readJdbcHelper.query(
                new SqlStatement(sql, new SqlParamsBuilder()),
                StaffAccountSummaryReadModel.class
        );
    }

    @Override
    public List<StaffAccountSummaryReadModel> findAll(StaffAccountStatus status, StaffAccountId enabledBy, StaffAccountId disabledBy, Instant createdBefore, Instant createdAfter) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, username, status, order_access_duration, modmail_transcript_access_duration, created_at_utc
                FROM staff_accounts
                WHERE 1=1
                """);

        SqlParamsBuilder params = new SqlParamsBuilder();

        if (status != null) {
            sql.append(" AND status = :status");
            params.add("status", status.name());
        }

        if (enabledBy != null) {
            sql.append(" AND enabled_by = :enabledBy");
            params.add("enabledBy", enabledBy.getValue());
        }

        if (disabledBy != null) {
            sql.append(" AND disabled_by = :disabledBy");
            params.add("disabledBy", disabledBy.getValue());
        }

        if (createdBefore != null) {
            sql.append(" AND created_at_utc < :createdBefore");
            params.add("createdBefore", createdBefore);
        }

        if (createdAfter != null) {
            sql.append(" AND created_at_utc > :createdAfter");
            params.add("createdAfter", createdAfter);
        }

        sql.append(" ORDER BY created_at_utc DESC");

        return readJdbcHelper.query(
                new SqlStatement(sql.toString(), params),
                StaffAccountSummaryReadModel.class
        );
    }

    @Override
    public Optional<StaffAccountSummaryReadModel> findByUsername(Username username) {
        String sql = """
                SELECT id, username, status, order_access_duration, modmail_transcript_access_duration, created_at_utc
                FROM staff_accounts
                WHERE username = :username
                """;

        SqlParamsBuilder params = new SqlParamsBuilder().add("username", username.getValue());
        return readJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                StaffAccountSummaryReadModel.class
        );
    }
}
