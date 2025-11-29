package com.paragon.helpers;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.infrastructure.persistence.daos.*;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TestJdbcHelper {
    private final WriteJdbcHelper writeJdbcHelper;

    public TestJdbcHelper(WriteJdbcHelper writeJdbcHelper) {
        this.writeJdbcHelper = writeJdbcHelper;
    }

    public void insertStaffAccount(StaffAccount staffAccount) {
        List<SqlStatement> queries = new ArrayList<>();

        String insertStaffAccountSql = """
                INSERT INTO staff_accounts (
                    id, username, email, password, is_password_temporary, password_issued_at_utc,
                    order_access_duration, modmail_transcript_access_duration,
                    status, failed_login_attempts, locked_until_utc, last_login_at_utc,
                    created_by, disabled_by, enabled_by, password_reset_by, version, created_at_utc, updated_at_utc
                ) VALUES (
                    :id, :username, :email, :password, :isPasswordTemporary, :passwordIssuedAtUtc,
                    :orderAccessDuration, :modmailTranscriptAccessDuration,
                    :status, :failedLoginAttempts, :lockedUntilUtc, :lastLoginAtUtc,
                    :createdBy, :disabledBy, :enabledBy, :passwordResetBy, :version, :createdAtUtc, :updatedAtUtc
                )
                """;

        SqlParamsBuilder insertStaffAccountParams = new SqlParamsBuilder()
                .add("id", staffAccount.getId().getValue())
                .add("username", staffAccount.getUsername().getValue())
                .add("email", staffAccount.getEmail() != null ? staffAccount.getEmail().getValue() : null)
                .add("password", staffAccount.getPassword().getValue())
                .add("isPasswordTemporary", staffAccount.isPasswordTemporary())
                .add("passwordIssuedAtUtc", staffAccount.getPasswordIssuedAt())
                .add("orderAccessDuration", staffAccount.getOrderAccessDuration().getValueInDays())
                .add("modmailTranscriptAccessDuration", staffAccount.getModmailTranscriptAccessDuration().getValueInDays())
                .add("status", staffAccount.getStatus().toString())
                .add("failedLoginAttempts", staffAccount.getFailedLoginAttempts().getValue())
                .add("lockedUntilUtc", staffAccount.getLockedUntil())
                .add("lastLoginAtUtc", staffAccount.getLastLoginAt())
                .add("createdBy", staffAccount.getCreatedBy().getValue())
                .add("disabledBy", staffAccount.getDisabledBy() != null ? staffAccount.getDisabledBy().getValue() : null)
                .add("enabledBy", staffAccount.getEnabledBy() != null ? staffAccount.getEnabledBy().getValue() : null)
                .add("passwordResetBy", staffAccount.getPasswordResetBy() != null ? staffAccount.getPasswordResetBy().getValue() : null)
                .add("version", staffAccount.getVersion().getValue())
                .add("createdAtUtc", Instant.now())
                .add("updatedAtUtc", Instant.now());

        queries.add(new SqlStatement(insertStaffAccountSql, insertStaffAccountParams));

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
            queries.add(new SqlStatement(joinTableSql, joinTableParams));
        }

        writeJdbcHelper.executeMultiple(queries);
    }

    public Optional<StaffAccount> getStaffAccountById(StaffAccountId staffAccountId) {
        String sql = "SELECT * FROM staff_accounts WHERE id = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", staffAccountId.getValue());

        Optional<StaffAccountDao> optionalDao =
                writeJdbcHelper.queryFirstOrDefault(
                        new SqlStatement(sql, params),
                        StaffAccountDao.class
                );

        return optionalDao.map(dao -> {
            List<PermissionCode> permissionCodes = getPermissionsForStaff(StaffAccountId.of(dao.id()));
            return dao.toStaffAccount(permissionCodes);
        });
    }

    public Optional<StaffAccount> getStaffAccountByUsername(Username username) {
        String sql = "SELECT * FROM staff_accounts WHERE username = :username";
        SqlParamsBuilder params = new SqlParamsBuilder().add("username", username.getValue());

        Optional<StaffAccountDao> optionalDao = writeJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                StaffAccountDao.class
        );

        return optionalDao.map(dao -> {
            List<PermissionCode> permissionCodes = getPermissionsForStaff(StaffAccountId.of(dao.id()));
            return dao.toStaffAccount(permissionCodes);
        });
    }

    public List<PermissionCode> getPermissionsForStaff(StaffAccountId staffAccountId) {
        String sql = "SELECT permission_code FROM staff_account_permissions WHERE staff_account_id = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", staffAccountId.getValue());

        List<PermissionCodeDao> daos = writeJdbcHelper.query(
                new SqlStatement(sql, params),
                PermissionCodeDao.class
        );
        return daos.stream().map(PermissionCodeDao::toPermissionCode).toList();
    }

    public void insertAuditTrailEntry(AuditTrailEntry auditTrailEntry) {
        String sql = """
            INSERT INTO audit_trail
            (id, actor_id, action_type, target_id, target_type, created_at_utc)
            VALUES
            (:id, :actorId, :actionType, :targetId, :targetType, :createdAtUtc)
        """;

        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("id", auditTrailEntry.getId().getValue())
                .add("actorId", auditTrailEntry.getActorId().getValue())
                .add("actionType", auditTrailEntry.getActionType().toString())
                .add("targetId", auditTrailEntry.getTargetId() != null ? auditTrailEntry.getTargetId().getValue() : null)
                .add("targetType", auditTrailEntry.getTargetType() != null ? auditTrailEntry.getTargetType().toString() : null)
                .add("createdAtUtc", Instant.now());

        writeJdbcHelper.execute(new SqlStatement(sql, params));
    }

    public Optional<AuditTrailEntry> getAuditTrailEntryById(AuditEntryId auditEntryId) {
        String sql = "SELECT * FROM audit_trail WHERE id = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", auditEntryId.getValue());

        Optional<AuditTrailEntryDao> optionalDao = writeJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                AuditTrailEntryDao.class
        );

        return optionalDao.map(AuditTrailEntryDao::toAuditTrailEntry);
    }

    public List<AuditTrailEntry> getAuditTrailEntriesByActorAndAction(StaffAccountId actorId, AuditEntryActionType actionType) {
        String sql = "SELECT * FROM audit_trail WHERE actor_id = :actorId AND action_type = :actionType";
        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("actorId", actorId.getValue())
                .add("actionType", actionType.toString());

        List<AuditTrailEntryDao> daos = writeJdbcHelper.query(
                new SqlStatement(sql, params),
                AuditTrailEntryDao.class
        );
        return daos.stream().map(AuditTrailEntryDao::toAuditTrailEntry).toList();
    }

    public void insertRefreshToken(RefreshToken refreshToken) {
        String sql = """
                INSERT INTO refresh_tokens
                (id, staff_account_id, token_hash, issued_from_ip_address, expires_at_utc, is_revoked,
                 revoked_at_utc, replaced_by, version, created_at_utc, updated_at_utc)
                VALUES
                (:id, :staffAccountId, :tokenHash, :issuedFromIpAddress, :expiresAtUtc, :isRevoked,
                 :revokedAtUtc, :replacedBy, :version, :createdAtUtc, :updatedAtUtc)
                """;

        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("id", refreshToken.getId().getValue())
                .add("staffAccountId", refreshToken.getStaffAccountId().getValue())
                .add("tokenHash", refreshToken.getTokenHash().getValue())
                .add("issuedFromIpAddress", refreshToken.getIssuedFromIpAddress().getValue())
                .add("expiresAtUtc", refreshToken.getExpiresAt())
                .add("isRevoked", refreshToken.isRevoked())
                .add("revokedAtUtc", refreshToken.getRevokedAt())
                .add("replacedBy", refreshToken.getReplacedBy() != null ? refreshToken.getReplacedBy().getValue() : null)
                .add("version", refreshToken.getVersion().getValue())
                .add("createdAtUtc", Instant.now())
                .add("updatedAtUtc", Instant.now());

        writeJdbcHelper.execute(new SqlStatement(sql, params));
    }

    public Optional<RefreshToken> getRefreshTokenById(RefreshTokenId refreshTokenId) {
        String sql = "SELECT * FROM refresh_tokens WHERE id = :id";
        SqlParamsBuilder params = new SqlParamsBuilder().add("id", refreshTokenId.getValue());

        Optional<RefreshTokenDao> optionalDao = writeJdbcHelper.queryFirstOrDefault(new SqlStatement(sql, params), RefreshTokenDao.class);
        return optionalDao.map(RefreshTokenDao::toRefreshToken);
    }

    public List<RefreshToken> getAllRefreshTokensByStaffAccountId(StaffAccountId staffAccountId) {
        String sql = "SELECT * FROM refresh_tokens WHERE staff_account_id = :staffAccountId";
        SqlParamsBuilder params = new SqlParamsBuilder().add("staffAccountId", staffAccountId.getValue());

        List<RefreshTokenDao> daos = writeJdbcHelper.query(
                new SqlStatement(sql, params),
                RefreshTokenDao.class
        );
        return daos.stream().map(RefreshTokenDao::toRefreshToken).toList();
    }

    public Optional<PasswordHistoryEntry> getPasswordHistoryEntryByHashedPassword(Password hashedPassword) {
        String sql = "SELECT * FROM staff_account_password_history WHERE hashed_password = :hashedPassword";
        SqlParamsBuilder params = new SqlParamsBuilder().add("hashedPassword", hashedPassword.getValue());
        Optional<PasswordHistoryEntryDao> optionalDao = writeJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                PasswordHistoryEntryDao.class
        );
        return optionalDao.map(PasswordHistoryEntryDao::toPasswordHistoryEntry);
    }

    public void insertPasswordHistoryEntry(PasswordHistoryEntry entry) {
        String sql = """
                        INSERT INTO staff_account_password_history
                        (id, staff_account_id, hashed_password, is_temporary, changed_at_utc)
                        VALUES
                        (:id, :staffAccountId, :hashedPassword, :isTemporary, :changedAtUtc)
                    """;
        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("id", UUID.randomUUID())
                .add("staffAccountId", entry.staffAccountId().getValue())
                .add("hashedPassword", entry.hashedPassword().getValue())
                .add("isTemporary", entry.isTemporary())
                .add("changedAtUtc", entry.changedAt().getValue());

        writeJdbcHelper.execute(new SqlStatement(sql, params));
    }
}
