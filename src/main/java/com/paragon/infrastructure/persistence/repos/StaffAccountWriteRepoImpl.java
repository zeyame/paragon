package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.repos.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.infrastructure.persistence.daos.PermissionCodeDao;
import com.paragon.infrastructure.persistence.daos.StaffAccountDao;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.SqlStatement;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StaffAccountWriteRepoImpl implements StaffAccountWriteRepo {
    private final WriteJdbcHelper jdbcHelper;

    public StaffAccountWriteRepoImpl(WriteJdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    public void create(StaffAccount staffAccount) {
        List<SqlStatement> sqlStatements = new ArrayList<>();

        String insertStaffAccountSql = """
            INSERT INTO staff_accounts
            (id, username, email, password, password_issued_at_utc,
             order_access_duration, modmail_transcript_access_duration,
             status, failed_login_attempts, locked_until_utc, last_login_at_utc, created_by,
             disabled_by, version, created_at_utc, updated_at_utc)
            VALUES
            (:id, :username, :email, :password, :passwordIssuedAtUtc,
             :orderAccessDuration, :modmailTranscriptAccessDuration,
             :status, :failedLoginAttempts, :lockedUntilUtc, :lastLoginAtUtc, :createdBy,
             :disabledBy, :version, :createdAtUtc, :updatedAtUtc)
        """;

        SqlParamsBuilder insertStaffAccountParams = new SqlParamsBuilder()
                .add("id", staffAccount.getId().getValue())
                .add("username", staffAccount.getUsername().getValue())
                .add("email", staffAccount.getEmail() != null ? staffAccount.getEmail().getValue() : null)
                .add("password", staffAccount.getPassword().getValue())
                .add("passwordIssuedAtUtc", staffAccount.getPasswordIssuedAt())
                .add("orderAccessDuration", staffAccount.getOrderAccessDuration().getValueInDays())
                .add("modmailTranscriptAccessDuration", staffAccount.getModmailTranscriptAccessDuration().getValueInDays())
                .add("status", staffAccount.getStatus().toString())
                .add("failedLoginAttempts", staffAccount.getFailedLoginAttempts().getValue())
                .add("lockedUntilUtc", staffAccount.getLockedUntil())
                .add("lastLoginAtUtc", staffAccount.getLastLoginAt())
                .add("createdBy", staffAccount.getCreatedBy().getValue())
                .add("disabledBy", staffAccount.getDisabledBy() != null ? staffAccount.getDisabledBy().getValue() : null)
                .add("version", staffAccount.getVersion().getValue())
                .add("createdAtUtc", Instant.now())
                .add("updatedAtUtc", Instant.now());

        sqlStatements.add(new SqlStatement(insertStaffAccountSql, insertStaffAccountParams));

        List<PermissionCode> permissionCodes = new ArrayList<>(staffAccount.getPermissionCodes());
        String joinTableSql = """
            INSERT INTO staff_account_permissions
            (staff_account_id, permission_code, assigned_by, assigned_at_utc)
            VALUES
            (:staffAccountId, :permissionCode, :assignedBy, :assignedAtUtc)
        """;
        for (PermissionCode code : permissionCodes) {
            SqlParamsBuilder joinTableParams = new SqlParamsBuilder()
                    .add("staffAccountId", staffAccount.getId().getValue())
                    .add("permissionCode", code.getValue())
                    .add("assignedBy", staffAccount.getCreatedBy().getValue())
                    .add("assignedAtUtc", Instant.now());
            sqlStatements.add(new SqlStatement(joinTableSql, joinTableParams));
        }

        jdbcHelper.executeMultiple(sqlStatements);
    }

    @Override
    public Optional<StaffAccount> getById(StaffAccountId staffAccountId) {
        String sql = "SELECT * FROM staff_accounts WHERE id = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", staffAccountId.getValue());

        Optional<StaffAccountDao> optional = jdbcHelper.queryFirstOrDefault(sql, params, StaffAccountDao.class);
        return optional.map(dao -> {
                List<PermissionCode> permissionCodes = getPermissionCodesBy(dao.id());
                return dao.toStaffAccount(permissionCodes);
        });
    }

    @Override
    public Optional<StaffAccount> getByUsername(Username username) {
        String sql = "SELECT * FROM staff_accounts WHERE username = :username";
        SqlParamsBuilder params = new SqlParamsBuilder().add("username", username.getValue());

        Optional<StaffAccountDao> optional = jdbcHelper.queryFirstOrDefault(sql, params, StaffAccountDao.class);
        return optional.map(dao -> {
            List<PermissionCode> permissionCodes = getPermissionCodesBy(dao.id());
            return dao.toStaffAccount(permissionCodes);
        });
    }

    private List<PermissionCode> getPermissionCodesBy(UUID staffAccountId) {
        String sql = "SELECT permission_code FROM staff_account_permissions WHERE staff_account_id = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", staffAccountId);

        List<PermissionCodeDao> permissionCodeDaos = jdbcHelper.query(sql, params, PermissionCodeDao.class);
        return permissionCodeDaos
                .stream()
                .map(PermissionCodeDao::toPermissionCode)
                .toList();
    }

    @Override
    public void update(StaffAccount staffAccount) {}
}
