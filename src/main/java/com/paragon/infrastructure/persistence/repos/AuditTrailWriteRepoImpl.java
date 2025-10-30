package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.repos.AuditTrailWriteRepo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.SqlStatement;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class AuditTrailWriteRepoImpl implements AuditTrailWriteRepo {
    private final WriteJdbcHelper jdbcHelper;

    public AuditTrailWriteRepoImpl(WriteJdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    @Override
    public void create(AuditTrailEntry auditTrailEntry) {
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

        jdbcHelper.execute(new SqlStatement(sql, params));
    }
}
