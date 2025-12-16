package com.paragon.infrastructure.persistence.repos.read;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountRequestReadRepo;
import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.jdbc.helpers.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountRequestReadModel;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StaffAccountRequestReadRepoImpl implements StaffAccountRequestReadRepo {
    private final ReadJdbcHelper readJdbcHelper;

    public StaffAccountRequestReadRepoImpl(ReadJdbcHelper readJdbcHelper) {
        this.readJdbcHelper = readJdbcHelper;
    }

    @Override
    public Optional<StaffAccountRequestReadModel> getPendingRequestBySubmitterAndType(StaffAccountId submitter, StaffAccountRequestType requestType) {
        String sql = """
                        SELECT
                              r.id,
                              r.submitted_by,
                              s1.username as submitted_by_username,
                              r.request_type,
                              r.target_id,
                              r.target_type,
                              r.status,
                              r.submitted_at_utc,
                              r.expires_at_utc,
                              r.approved_by,
                              s2.username as approved_by_username,
                              r.approved_at_utc,
                              r.rejected_by,
                              s3.username as rejected_by_username,
                              r.rejected_at_utc
                          FROM staff_account_requests r
                          INNER JOIN staff_accounts s1 ON r.submitted_by = s1.id
                          LEFT JOIN staff_accounts s2 ON r.approved_by = s2.id
                          LEFT JOIN staff_accounts s3 ON r.rejected_by = s3.id
                          WHERE r.submitted_by = :submittedBy
                            AND r.request_type = :requestType
                            AND r.status = :status
                """;
        SqlParamsBuilder params = new SqlParamsBuilder()
                .add("submittedBy", submitter.getValue())
                .add("requestType", requestType.toString())
                .add("status", StaffAccountRequestStatus.PENDING.toString());

        return readJdbcHelper.queryFirstOrDefault(new SqlStatement(sql, params), StaffAccountRequestReadModel.class);
    }
}
