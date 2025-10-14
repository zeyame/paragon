package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.AuditTrailWriteRepo;
import com.paragon.domain.models.entities.AuditTrailEntry;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
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
            (id, actor_id, action_type, target_id, target_type, outcome, ip_address, correlation_id, created_at_utc)
            VALUES
            (:id, :actorId, :actionType, :targetId, :targetType, :outcome, :ipAddress, :correlationId, :createdAtUtc)
        """;

        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("id", auditTrailEntry.getId().getValue())
                .add("actorId", auditTrailEntry.getActorId().getValue())
                .add("actionType", auditTrailEntry.getActionType().toString())
                .add("targetId", auditTrailEntry.getTargetId() != null ? auditTrailEntry.getTargetId().getValue() : null)
                .add("targetType", auditTrailEntry.getTargetType() != null ? auditTrailEntry.getTargetType().toString() : null)
                .add("outcome", auditTrailEntry.getOutcome().toString())
                .add("ipAddress", auditTrailEntry.getIpAddress())
                .add("correlationId", auditTrailEntry.getCorrelationId())
                .add("createdAtUtc", Instant.now());

        jdbcHelper.execute(sql, params);
    }
}
