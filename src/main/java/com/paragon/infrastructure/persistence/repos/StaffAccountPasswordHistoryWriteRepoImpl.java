package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.StaffAccountPasswordHistoryWriteRepo;
import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class StaffAccountPasswordHistoryWriteRepoImpl implements StaffAccountPasswordHistoryWriteRepo {
    private final WriteJdbcHelper writeJdbcHelper;

    public StaffAccountPasswordHistoryWriteRepoImpl(WriteJdbcHelper writeJdbcHelper) {
        this.writeJdbcHelper = writeJdbcHelper;
    }

    @Override
    public void appendEntry(PasswordHistoryEntry entry) {
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
