package com.paragon.helpers;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.AuditEntryId;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.daos.AuditTrailEntryDao;
import com.paragon.infrastructure.persistence.daos.PermissionCodeDao;
import com.paragon.infrastructure.persistence.daos.StaffAccountDao;
import com.paragon.infrastructure.persistence.jdbc.SqlParams;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.WriteQuery;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TestJdbcHelper {
    private final WriteJdbcHelper writeJdbcHelper;

    public TestJdbcHelper(WriteJdbcHelper writeJdbcHelper) {
        this.writeJdbcHelper = writeJdbcHelper;
    }

    public void insertStaffAccount(StaffAccount staffAccount) {
        List<WriteQuery> queries = new ArrayList<>();

        String insertStaffAccountSql = """
                INSERT INTO staff_accounts (
                    id, username, email, password, password_issued_at_utc,
                    order_access_duration, modmail_transcript_access_duration,
                    status, failed_login_attempts, locked_until_utc, last_login_at_utc,
                    created_by, disabled_by, version, created_at_utc, updated_at_utc
                ) VALUES (
                    :id, :username, :email, :password, :passwordIssuedAtUtc,
                    :orderAccessDuration, :modmailTranscriptAccessDuration,
                    :status, :failedLoginAttempts, :lockedUntilUtc, :lastLoginAtUtc,
                    :createdBy, :disabledBy, :version, :createdAtUtc, :updatedAtUtc
                )
                """;

        SqlParams insertStaffAccountParams = new SqlParams()
                .add("id", staffAccount.getId().getValue())
                .add("username", staffAccount.getUsername().getValue())
                .add("email", staffAccount.getEmail().getValue())
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

        queries.add(new WriteQuery(insertStaffAccountSql, insertStaffAccountParams));

        List<PermissionCode> permissionCodes = new ArrayList<>(staffAccount.getPermissionCodes());
        String joinTableSql = """
            INSERT INTO staff_account_permissions
            (staff_account_id, permission_code, assigned_by, assigned_at_utc)
            VALUES
            (:staffAccountId, :permissionCode, :assignedBy, :assignedAtUtc)
        """;
        for (PermissionCode code : permissionCodes) {
            SqlParams joinTableParams = new SqlParams()
                    .add("staffAccountId", staffAccount.getId().getValue())
                    .add("permissionCode", code.getValue())
                    .add("assignedBy", staffAccount.getCreatedBy().getValue())
                    .add("assignedAtUtc", Instant.now());
            queries.add(new WriteQuery(joinTableSql, joinTableParams));
        }

        writeJdbcHelper.executeMultiple(queries);
    }

    public Optional<StaffAccount> getStaffAccountById(StaffAccountId staffAccountId) {
        String sql = "SELECT * FROM staff_accounts WHERE id = :id";
        SqlParams params = new SqlParams().add("id", staffAccountId.getValue());

        Optional<StaffAccountDao> optionalDao =
                writeJdbcHelper.queryFirstOrDefault(sql, params, StaffAccountDao.class);

        return optionalDao.map(dao -> {
            List<PermissionCode> permissionCodes = getPermissionsForStaff(StaffAccountId.of(dao.id()));
            return dao.toStaffAccount(permissionCodes);
        });
    }

    public Optional<StaffAccount> getStaffAccountByUsername(Username username) {
        String sql = "SELECT * FROM staff_accounts WHERE username = :username";
        SqlParams params = new SqlParams().add("username", username.getValue());

        Optional<StaffAccountDao> optionalDao = writeJdbcHelper.queryFirstOrDefault(sql, params, StaffAccountDao.class);

        return optionalDao.map(dao -> {
            List<PermissionCode> permissionCodes = getPermissionsForStaff(StaffAccountId.of(dao.id()));
            return dao.toStaffAccount(permissionCodes);
        });
    }

    public List<PermissionCode> getPermissionsForStaff(StaffAccountId staffAccountId) {
        String sql = "SELECT permission_code FROM staff_account_permissions WHERE staff_account_id = :id";
        SqlParams params = new SqlParams().add("id", staffAccountId.getValue());

        List<PermissionCodeDao> daos = writeJdbcHelper.query(sql, params, PermissionCodeDao.class);
        return daos.stream().map(PermissionCodeDao::toPermissionCode).toList();
    }

    public void insertAuditTrailEntry(AuditTrailEntry auditTrailEntry) {
        String sql = """
            INSERT INTO audit_trail
            (id, actor_id, action_type, target_id, target_type, outcome, ip_address, correlation_id, created_at_utc)
            VALUES
            (:id, :actorId, :actionType, :targetId, :targetType, :outcome, :ipAddress, :correlationId, :createdAtUtc)
        """;

        SqlParams params = new SqlParams()
                .add("id", auditTrailEntry.getId().getValue())
                .add("actorId", auditTrailEntry.getActorId().getValue())
                .add("actionType", auditTrailEntry.getActionType().toString())
                .add("targetId", auditTrailEntry.getTargetId() != null ? auditTrailEntry.getTargetId().getValue() : null)
                .add("targetType", auditTrailEntry.getTargetType() != null ? auditTrailEntry.getTargetType().toString() : null)
                .add("outcome", auditTrailEntry.getOutcome().toString())
                .add("ipAddress", auditTrailEntry.getIpAddress())
                .add("correlationId", auditTrailEntry.getCorrelationId())
                .add("createdAtUtc", Instant.now());

        writeJdbcHelper.execute(sql, params);
    }

    public Optional<AuditTrailEntry> getAuditTrailEntryById(AuditEntryId auditEntryId) {
        String sql = "SELECT * FROM audit_trail WHERE id = :id";
        SqlParams params = new SqlParams().add("id", auditEntryId.getValue());

        Optional<AuditTrailEntryDao> optionalDao = writeJdbcHelper.queryFirstOrDefault(sql, params, AuditTrailEntryDao.class);

        return optionalDao.map(AuditTrailEntryDao::toAuditTrailEntry);
    }

    public List<AuditTrailEntry> getAuditTrailEntriesByActorAndAction(StaffAccountId actorId, AuditEntryActionType actionType) {
        String sql = "SELECT * FROM audit_trail WHERE actor_id = :actorId AND action_type = :actionType";
        SqlParams params = new SqlParams()
                .add("actorId", actorId.getValue())
                .add("actionType", actionType.toString());

        List<AuditTrailEntryDao> daos = writeJdbcHelper.query(sql, params, AuditTrailEntryDao.class);
        return daos.stream().map(AuditTrailEntryDao::toAuditTrailEntry).toList();
    }
}
