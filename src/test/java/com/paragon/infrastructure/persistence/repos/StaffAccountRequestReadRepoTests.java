package com.paragon.infrastructure.persistence.repos;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountRequestReadRepo;
import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.StaffAccountRequestReadModelFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.helpers.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountRequestReadModel;
import com.paragon.infrastructure.persistence.repos.read.StaffAccountRequestReadRepoImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class StaffAccountRequestReadRepoTests {
    private final StaffAccountRequestReadRepo sut;
    private final ReadJdbcHelper readJdbcHelperMock;

    public StaffAccountRequestReadRepoTests() {
        readJdbcHelperMock = mock(ReadJdbcHelper.class);
        sut = new StaffAccountRequestReadRepoImpl(readJdbcHelperMock);
    }
    @Nested
    class GetPendingRequestBySubmitterAndType {
        @Test
        void shouldExecuteQueryWithCorrectSqlAndParams() {
            // Given
            StaffAccountId submitter = StaffAccountId.generate();
            StaffAccountRequestType requestType = StaffAccountRequestType.PASSWORD_CHANGE;

            String expectedSql = """
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

            // When
            sut.getPendingRequestBySubmitterAndType(submitter, requestType);

            // Then
            ArgumentCaptor<SqlStatement> sqlStatementArgumentCaptor = ArgumentCaptor.forClass(SqlStatement.class);
            verify(readJdbcHelperMock, times(1))
                    .queryFirstOrDefault(sqlStatementArgumentCaptor.capture(), eq(StaffAccountRequestReadModel.class));

            SqlStatement capturedSqlStatement = sqlStatementArgumentCaptor.getValue();
            assertThat(capturedSqlStatement.sql()).isEqualToIgnoringWhitespace(expectedSql);
            assertThat(capturedSqlStatement.params().get("submittedBy")).isEqualTo(submitter.getValue());
            assertThat(capturedSqlStatement.params().get("requestType")).isEqualTo(requestType.toString());
            assertThat(capturedSqlStatement.params().get("status")).isEqualTo(StaffAccountRequestStatus.PENDING.toString());
        }

        @Test
        void shouldReturnExpectedResponse_whenPendingRequestExists() {
            // Given
            StaffAccountRequestReadModel expectedModel = StaffAccountRequestReadModelFixture.validPendingRequest();
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountRequestReadModel.class)))
                    .thenReturn(Optional.of(expectedModel));

            // When
            Optional<StaffAccountRequestReadModel> optionalModel = sut.getPendingRequestBySubmitterAndType(
                    StaffAccountId.of(expectedModel.submittedBy()), expectedModel.requestType());

            // Then
            assertThat(optionalModel).isPresent();
            assertThat(optionalModel.get()).isEqualTo(expectedModel);
        }

        @Test
        void shouldReturnEmptyOptional_whenPendingRequestDoesNotExist() {
            // Given
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountRequestReadModel.class)))
                    .thenReturn(Optional.empty());

            // When
            Optional<StaffAccountRequestReadModel> optionalModel = sut.getPendingRequestBySubmitterAndType(
                    StaffAccountId.generate(), StaffAccountRequestType.PASSWORD_CHANGE
            );

            // Then
            assertThat(optionalModel).isEmpty();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountRequestReadModel.class)))
                    .thenThrow(mock(InfraException.class));

            // When & Then
            assertThatExceptionOfType(InfraException.class)
                    .isThrownBy(() -> sut.getPendingRequestBySubmitterAndType(StaffAccountId.generate(), StaffAccountRequestType.PASSWORD_CHANGE));
        }
    }
}
