package com.paragon.infrastructure.persistence.repos.write;

import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.interfaces.repositories.StaffAccountRequestWriteRepo;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.daos.StaffAccountRequestIdDao;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class StaffAccountRequestWriteRepoImpl implements StaffAccountRequestWriteRepo {
    private final WriteJdbcHelper writeJdbcHelper;

    public StaffAccountRequestWriteRepoImpl(WriteJdbcHelper writeJdbcHelper) {
        this.writeJdbcHelper = writeJdbcHelper;
    }

    @Override
    public void create(StaffAccountRequest request) {
        String sql = """
            INSERT INTO staff_account_requests
            (id, submitted_by, request_type, target_id, target_type, status, submitted_at_utc,
             expires_at_utc, approved_by, approved_at_utc, rejected_by, rejected_at_utc,
             version, updated_at_utc)
            VALUES
            (:id, :submittedBy, :requestType, :targetId, :targetType, :status, :submittedAtUtc,
             :expiresAtUtc, :approvedBy, :approvedAtUtc, :rejectedBy, :rejectedAtUtc,
             :version, :updatedAtUtc)
        """;

        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("id", request.getId().getValue())
                .add("submittedBy", request.getSubmittedBy().getValue())
                .add("requestType", request.getRequestType().toString())
                .add("targetId", request.getTargetId() != null ? request.getTargetId().getValue() : null)
                .add("targetType", request.getTargetType() != null ? request.getTargetType().toString() : null)
                .add("status", request.getStatus().toString())
                .add("submittedAtUtc", request.getSubmittedAt().getValue())
                .add("expiresAtUtc", request.getExpiresAt().getValue())
                .add("approvedBy", request.getApprovedBy() != null ? request.getApprovedBy().getValue() : null)
                .add("approvedAtUtc", request.getApprovedAt() != null ? request.getApprovedAt().getValue() : null)
                .add("rejectedBy", request.getRejectedBy() != null ? request.getRejectedBy().getValue() : null)
                .add("rejectedAtUtc", request.getRejectedAt() != null ? request.getRejectedAt().getValue() : null)
                .add("version", request.getVersion().getValue())
                .add("updatedAtUtc", Instant.now());

        writeJdbcHelper.execute(new SqlStatement(sql, params));
    }

    @Override
    public boolean existsPendingRequestBySubmitterAndType(StaffAccountId submitter, StaffAccountRequestType requestType) {
        String sql = """
            SELECT id FROM staff_account_requests
            WHERE submitted_by = :submittedBy
            AND request_type = :requestType
            AND status = :status
        """;

        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("submittedBy", submitter.getValue())
                .add("requestType", requestType.toString())
                .add("status", StaffAccountRequestStatus.PENDING.toString());

        return writeJdbcHelper.queryFirstOrDefault(
                new SqlStatement(sql, params),
                StaffAccountRequestIdDao.class
        ).isPresent();
    }
}
