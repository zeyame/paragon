package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.StaffAccountRequestWriteRepo;
import com.paragon.helpers.fixtures.StaffAccountRequestFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class StaffAccountRequestWriteRepoTests {
    @Nested
    class Create {
        private final StaffAccountRequestWriteRepo sut;
        private final WriteJdbcHelper writeJdbcHelperMock;

        public Create() {
            writeJdbcHelperMock = mock(WriteJdbcHelper.class);
            sut = new StaffAccountRequestWriteRepoImpl(writeJdbcHelperMock);
        }

        @Test
        void shouldExecuteWithCorrectSqlAndParams() {
            // Given
            var request = StaffAccountRequestFixture.validStaffAccountRequest();
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            String expectedSql = """
                INSERT INTO staff_account_requests
                (id, submitted_by, request_type, target_id, target_type, status, submitted_at_utc,
                 expires_at_utc, approved_by, approved_at_utc, rejected_by, rejected_at_utc,
                 version, updated_at_utc)
                VALUES
                (:id, :submittedBy, :requestType, :targetId, :targetType, :status, :submittedAtUtc,
                 :expiresAtUtc, :approvedBy, :approvedAtUtc, :rejectedBy, :rejectedAtUtc,
                 :version, :updatedAtUtc)
            """;

            // When
            sut.create(request);

            // Then
            verify(writeJdbcHelperMock, times(1)).execute(sqlStatementCaptor.capture());
            SqlStatement insertStatement = sqlStatementCaptor.getValue();
            var params = insertStatement.params().build();

            assertThat(insertStatement.sql()).isEqualToIgnoringWhitespace(expectedSql);
            assertThat(params.get("id")).isEqualTo(request.getId().getValue());
            assertThat(params.get("submittedBy")).isEqualTo(request.getSubmittedBy().getValue());
            assertThat(params.get("requestType")).isEqualTo(request.getRequestType().toString());
            assertThat(params.get("targetId")).isNull();
            assertThat(params.get("targetType")).isNull();
            assertThat(params.get("status")).isEqualTo(request.getStatus().toString());
            assertThat(params.get("submittedAtUtc")).isEqualTo(Timestamp.from(request.getSubmittedAt().getValue()));
            assertThat(params.get("expiresAtUtc")).isEqualTo(Timestamp.from(request.getExpiresAt().getValue()));
            assertThat(params.get("approvedBy")).isNull();
            assertThat(params.get("approvedAtUtc")).isNull();
            assertThat(params.get("rejectedBy")).isNull();
            assertThat(params.get("rejectedAtUtc")).isNull();
            assertThat(params.get("version")).isEqualTo(request.getVersion().getValue());
            assertThat(params.get("updatedAtUtc")).isNotNull();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            doThrow(InfraException.class)
                    .when(writeJdbcHelperMock)
                    .execute(any(SqlStatement.class));

            // When & Then
            assertThatThrownBy(() -> sut.create(StaffAccountRequestFixture.validStaffAccountRequest()))
                    .isInstanceOf(InfraException.class);
        }
    }
}
