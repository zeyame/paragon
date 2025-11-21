package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.RefreshTokenWriteRepo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.daos.RefreshTokenDao;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RefreshTokenWriteRepoImpl implements RefreshTokenWriteRepo {
    private final WriteJdbcHelper jdbcHelper;

    public RefreshTokenWriteRepoImpl(WriteJdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    @Override
    public void create(RefreshToken refreshToken) {
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

        jdbcHelper.execute(new SqlStatement(sql, params));
    }

    @Override
    public List<RefreshToken> getActiveTokensByStaffAccountId(StaffAccountId staffAccountId) {
        String sql = """
                        SELECT * FROM refresh_tokens
                        WHERE staff_account_id = :staffAccountId
                        AND expires_at_utc > :now
                        AND is_revoked = :isRevoked
                    """;
        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("staffAccountId", staffAccountId.getValue())
                .add("now", Instant.now())
                .add("isRevoked", false);

        List<RefreshTokenDao> refreshTokenDaos = jdbcHelper.query(
                new SqlStatement(sql, params),
                RefreshTokenDao.class
        );
        return refreshTokenDaos.stream()
                .map(RefreshTokenDao::toRefreshToken)
                .toList();
    }

    @Override
    public void update(RefreshToken refreshToken) {
        String sql = """
                    UPDATE refresh_tokens
                    SET is_revoked = :isRevoked, revoked_at_utc = :revokedAtUtc, replaced_by = :replacedBy, version = :version, updated_at_utc = :updatedAtUtc
                    WHERE id = :id
                    """;

        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("id", refreshToken.getId().getValue())
                .add("isRevoked", refreshToken.isRevoked())
                .add("revokedAtUtc", refreshToken.getRevokedAt())
                .add("replacedBy", refreshToken.getReplacedBy() != null ? refreshToken.getReplacedBy().getValue() : null)
                .add("version", refreshToken.getVersion().getValue())
                .add("updatedAtUtc", Instant.now());

        int affectedRows = jdbcHelper.execute(new SqlStatement(sql, params));
        if (affectedRows != 1) {
            throw new InfraException(); // potential concurrency detected
        }
    }

    @Override
    public void updateAll(List<RefreshToken> refreshTokens) {
        String sql = """
                    UPDATE refresh_tokens
                    SET is_revoked = :isRevoked, revoked_at_utc = :revokedAtUtc, replaced_by = :replacedBy, version = :version, updated_at_utc = :updatedAtUtc
                    WHERE id = :id
                    """;

        List<SqlStatement> sqlStatements = new ArrayList<>();
        for (RefreshToken token : refreshTokens) {
            SqlParamsBuilder params = new SqlParamsBuilder()
                    .add("id", token.getId().getValue())
                    .add("isRevoked", token.isRevoked())
                    .add("revokedAtUtc", token.getRevokedAt())
                    .add("replacedBy", token.getReplacedBy() != null ? token.getReplacedBy().getValue() : null)
                    .add("version", token.getVersion().getValue())
                    .add("updatedAtUtc", Instant.now());
            sqlStatements.add(new SqlStatement(sql, params));
        }

        jdbcHelper.executeMultiple(sqlStatements);
    }
}
