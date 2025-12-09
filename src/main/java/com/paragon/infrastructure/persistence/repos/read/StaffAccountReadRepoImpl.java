package com.paragon.infrastructure.persistence.repos.read;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.valueobjects.DateTimeUtc;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.daos.*;
import com.paragon.infrastructure.persistence.jdbc.helpers.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountDetailedReadModel;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StaffAccountReadRepoImpl implements StaffAccountReadRepo {
    private final ReadJdbcHelper readJdbcHelper;

    public StaffAccountReadRepoImpl(ReadJdbcHelper readJdbcHelper) {
        this.readJdbcHelper = readJdbcHelper;
    }

    @Override
    public boolean exists(UUID staffAccountId) {
        String sql = "SELECT id FROM staff_accounts WHERE id = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", staffAccountId);

        return readJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                StaffAccountIdDao.class
        ).isPresent();
    }

    @Override
    public boolean hasPermission(UUID staffAccountId, PermissionCode permissionCode) {
        String sql = "SELECT * FROM staff_account_permissions WHERE staff_account_id = :staffAccountId AND permission_code = :permissionCode";
        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("staffAccountId", staffAccountId)
                .add("permissionCode", permissionCode.getValue());

        return readJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                StaffAccountPermissionDao.class
        ).isPresent();
    }

    @Override
    public List<StaffAccountSummaryReadModel> findAllSummaries(StaffAccountStatus status,
                                                               Username enabledBy,
                                                               Username disabledBy,
                                                               DateTimeUtc createdBefore,
                                                               DateTimeUtc createdAfter) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, username, status, created_at_utc
                FROM staff_accounts
                WHERE 1=1
                """);

        SqlParamsBuilder params = new SqlParamsBuilder();

        if (status != null) {
            sql.append(" AND status = :status");
            params.add("status", status.name());
        }

        if (enabledBy != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM staff_accounts enabler WHERE enabler.id = staff_accounts.enabled_by AND enabler.username = :enabledBy)");
            params.add("enabledBy", enabledBy.getValue());
        }

        if (disabledBy != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM staff_accounts disabler WHERE disabler.id = staff_accounts.disabled_by AND disabler.username = :disabledBy)");
            params.add("disabledBy", disabledBy.getValue());
        }

        if (createdBefore != null) {
            sql.append(" AND created_at_utc < :createdBefore");
            params.add("createdBefore", createdBefore.getValue());
        }

        if (createdAfter != null) {
            sql.append(" AND created_at_utc > :createdAfter");
            params.add("createdAfter", createdAfter.getValue());
        }

        sql.append(" ORDER BY created_at_utc DESC");

        return readJdbcHelper.query(
                new SqlStatement(sql.toString(), params),
                StaffAccountSummaryReadModel.class
        );
    }

    @Override
    public Optional<StaffAccountSummaryReadModel> findSummaryByUsername(String username) {
        String sql = """
                SELECT id, username, status, created_at_utc
                FROM staff_accounts
                WHERE username = :username
                """;

        SqlParamsBuilder params = new SqlParamsBuilder().add("username", username);
        return readJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                StaffAccountSummaryReadModel.class
        );
    }

    @Override
    public Optional<StaffAccountDetailedReadModel> findDetailedById(UUID staffAccountId) {
        String sql = """
                        SELECT
                            id,
                            username,
                            order_access_duration AS order_access_duration_in_days,
                            modmail_transcript_access_duration AS modmail_transcript_access_duration_in_days,
                            status,
                            locked_until_utc,
                            last_login_at_utc,
                            created_by,
                            disabled_by,
                            created_at_utc
                        FROM staff_accounts
                        WHERE id = :id
                    """;
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", staffAccountId);

        Optional<StaffAccountDetailedReadModelDao> optionalDao = readJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                StaffAccountDetailedReadModelDao.class
        );
        if (optionalDao.isEmpty()) {
            return Optional.empty();
        }

        List<String> permissionCodes = getPermissionCodesBy(staffAccountId);
        return Optional.of(StaffAccountDetailedReadModel.from(optionalDao.get(), permissionCodes));
    }

    @Override
    public Optional<StaffAccountStatus> findStatusById(UUID staffAccountId) {
        String sql = """
                        SELECT status FROM staff_accounts
                        WHERE id = :id
                    """;
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", staffAccountId);
        return readJdbcHelper
                .queryFirstOrDefault(new SqlStatement(sql, params), StaffAccountStatusDao.class)
                .map(StaffAccountStatusDao::toEnum);
    }

    private List<String> getPermissionCodesBy(UUID staffAccountId) {
        String sql = "SELECT permission_code FROM staff_account_permissions WHERE staff_account_id = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", staffAccountId);

        List<PermissionCodeDao> permissionCodeDaos = readJdbcHelper.query(
                new SqlStatement(sql, params),
                PermissionCodeDao.class
        );
        return permissionCodeDaos
                .stream()
                .map(dao -> dao.toPermissionCode().getValue())
                .toList();
    }
}
