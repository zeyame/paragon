package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.StaffAccountWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.infrastructure.persistence.daos.PermissionIdDao;
import com.paragon.infrastructure.persistence.daos.StaffAccountDao;
import com.paragon.infrastructure.persistence.jdbc.SqlParams;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.WriteQuery;
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
        List<WriteQuery> queries = new ArrayList<>();

        String accountSql = """
            INSERT INTO staff_accounts
            (id, username, email, password, password_issued_at,
             order_access_duration, modmail_transcript_access_duration,
             status, failed_login_attempts, locked_until, created_by,
             disabled_by, version, created_at_utc, updated_at_utc)
            VALUES
            (:id, :username, :email, :password, :passwordIssuedAt,
             :orderAccessDuration, :modmailTranscriptAccessDuration,
             :status, :failedLoginAttempts, :lockedUntil, :createdBy,
             :disabledBy, :version, :createdAtUtc, :updatedAtUtc)
        """;

        SqlParams accountParams = new SqlParams()
                .add("id", staffAccount.getId().getValue())
                .add("username", staffAccount.getUsername().getValue())
                .add("email", staffAccount.getEmail().getValue())
                .add("password", staffAccount.getPassword().getValue())
                .add("passwordIssuedAt", staffAccount.getPasswordIssuedAt())
                .add("orderAccessDuration", staffAccount.getOrderAccessDuration().getValue())
                .add("modmailTranscriptAccessDuration", staffAccount.getModmailTranscriptAccessDuration().getValue())
                .add("status", staffAccount.getStatus().toString())
                .add("failedLoginAttempts", staffAccount.getFailedLoginAttempts().getValue())
                .add("lockedUntil", staffAccount.getLockedUntil())
                .add("createdBy", staffAccount.getCreatedBy().getValue())
                .add("disabledBy", staffAccount.getDisabledBy() != null ? staffAccount.getDisabledBy().getValue() : null)
                .add("version", staffAccount.getVersion().getValue())
                .add("createdAtUtc", Instant.now())
                .add("updatedAtUtc", Instant.now());

        queries.add(new WriteQuery(accountSql, accountParams));

        List<PermissionId> permissionIds = new ArrayList<>(staffAccount.getPermissionIds());
        String joinTableSql = """
            INSERT INTO staff_account_permissions
            (staff_account_id, permission_id, assigned_by, assigned_at_utc)
            VALUES
            (:staffAccountId, :permissionId, :assignedBy, :assignedAtUtc)
        """;
        for (PermissionId id : permissionIds) {
            SqlParams joinTableParams = new SqlParams()
                    .add("staffAccountId", staffAccount.getId().getValue())
                    .add("permissionId", id.getValue())
                    .add("assignedBy", staffAccount.getCreatedBy())
                    .add("assignedAtUtc", Instant.now());
            queries.add(new WriteQuery(joinTableSql, joinTableParams));
        }

        jdbcHelper.executeMultiple(queries);
    }

    @Override
    public Optional<StaffAccount> getById(StaffAccountId staffAccountId) {
        String sql = "SELECT * FROM staff_accounts WHERE id = :id";
        SqlParams params = new SqlParams().add("id", staffAccountId.getValue());

        Optional<StaffAccountDao> optional = jdbcHelper.queryFirstOrDefault(sql, params, StaffAccountDao.class);
        return optional.map(dao -> {
                List<PermissionId> permissionIds = getPermissionsIdsBy(dao.id());
                return dao.toStaffAccount(permissionIds);
        });
    }


    private List<PermissionId> getPermissionsIdsBy(UUID staffAccountId) {
        String sql = "SELECT permission_id FROM staff_account_permissions WHERE staff_account_id = :id";
        SqlParams params = new SqlParams().add("id", staffAccountId);

        List<PermissionIdDao> permissionIdDaos = jdbcHelper.query(sql, params, PermissionIdDao.class);
        return permissionIdDaos
                .stream()
                .map(PermissionIdDao::toPermissionId)
                .toList();
    }
}
