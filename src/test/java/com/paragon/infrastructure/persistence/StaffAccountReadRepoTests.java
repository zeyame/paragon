package com.paragon.infrastructure.persistence;

import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.daos.StaffAccountIdDao;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.SqlStatement;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import com.paragon.infrastructure.persistence.repos.StaffAccountReadRepoImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class StaffAccountReadRepoTests {
    @Nested
    class Exists {
        private final ReadJdbcHelper readJdbcHelperMock;
        private final StaffAccountReadRepoImpl sut;

        public Exists() {
            this.readJdbcHelperMock = mock(ReadJdbcHelper.class);
            this.sut = new StaffAccountReadRepoImpl(readJdbcHelperMock);
        }

        @Test
        void callsJdbcHelper_withExpectedSqlAndParams() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountIdDao.class)))
                    .thenReturn(Optional.empty());

            // When
            sut.exists(staffAccountId);

            // Then
            verify(readJdbcHelperMock, times(1)).queryFirstOrDefault(sqlStatementCaptor.capture(), eq(StaffAccountIdDao.class));

            SqlStatement statement = sqlStatementCaptor.getValue();

            assertThat(statement.sql()).isEqualTo("SELECT id FROM staff_accounts WHERE id = :id");
            assertThat(statement.params().build().get("id")).isEqualTo(staffAccountId.getValue());
        }

        @Test
        void returnsTrue_whenStaffAccountExists() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            StaffAccountIdDao dao = new StaffAccountIdDao(staffAccountId.getValue());
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountIdDao.class)))
                    .thenReturn(Optional.of(dao));

            // When
            boolean result = sut.exists(staffAccountId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void returnsFalse_whenStaffAccountDoesNotExist() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountIdDao.class)))
                    .thenReturn(Optional.empty());

            // When
            boolean result = sut.exists(staffAccountId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountIdDao.class)))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.exists(staffAccountId))
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class HasPermission {
        private final ReadJdbcHelper readJdbcHelperMock;
        private final StaffAccountReadRepoImpl sut;

        public HasPermission() {
            this.readJdbcHelperMock = mock(ReadJdbcHelper.class);
            this.sut = new StaffAccountReadRepoImpl(readJdbcHelperMock);
        }

        @Test
        void callsJdbcHelper_withExpectedSqlAndParams() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), any()))
                    .thenReturn(Optional.empty());

            // When
            sut.hasPermission(staffAccountId, permissionCode);

            // Then
            verify(readJdbcHelperMock, times(1)).queryFirstOrDefault(sqlStatementCaptor.capture(), any());

            SqlStatement statement = sqlStatementCaptor.getValue();

            assertThat(statement.sql()).isEqualTo("SELECT * FROM staff_account_permissions WHERE staff_account_id = :staffAccountId AND permission_code = :permissionCode");
            assertThat(statement.params().build().get("staffAccountId")).isEqualTo(staffAccountId.getValue());
            assertThat(statement.params().build().get("permissionCode")).isEqualTo(permissionCode.getValue());
        }

        @Test
        void returnsTrue_whenStaffAccountHasPermission() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), any()))
                    .thenReturn(Optional.of(mock(Object.class)));

            // When
            boolean result = sut.hasPermission(staffAccountId, permissionCode);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void returnsFalse_whenStaffAccountDoesNotHavePermission() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), any()))
                    .thenReturn(Optional.empty());

            // When
            boolean result = sut.hasPermission(staffAccountId, permissionCode);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), any()))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.hasPermission(staffAccountId, permissionCode))
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class FindAllSummaries {
        private final ReadJdbcHelper readJdbcHelperMock;
        private final StaffAccountReadRepoImpl sut;

        public FindAllSummaries() {
            this.readJdbcHelperMock = mock(ReadJdbcHelper.class);
            this.sut = new StaffAccountReadRepoImpl(readJdbcHelperMock);
        }

        @Test
        void callsJdbcHelper_withExpectedSql() {
            // Given
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            when(readJdbcHelperMock.query(any(SqlStatement.class), any()))
                    .thenReturn(List.of());

            // When
            sut.findAllSummaries();

            // Then
            verify(readJdbcHelperMock, times(1)).query(sqlStatementCaptor.capture(), any());

            SqlStatement statement = sqlStatementCaptor.getValue();
            assertThat(statement.sql()).isEqualTo("SELECT id, username, status, order_access_duration, modmail_transcript_access_duration, created_at_utc FROM staff_accounts ORDER BY created_at_utc DESC");
        }

        @Test
        void returnsListOfSummaries_whenStaffAccountsExist() {
            // Given
            StaffAccountSummaryReadModel summary1 = new StaffAccountSummaryReadModel(
                    UUID.randomUUID(),
                    "john_doe",
                    "active",
                    10,
                    5,
                    java.time.Instant.now()
            );
            StaffAccountSummaryReadModel summary2 = new StaffAccountSummaryReadModel(
                    UUID.randomUUID(),
                    "jane_smith",
                    "pending_password_change",
                    14,
                    7,
                    java.time.Instant.now()
            );
            List<StaffAccountSummaryReadModel> expectedSummaries = List.of(summary1, summary2);

            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(expectedSummaries);

            // When
            var actualSummaries = sut.findAllSummaries();

            // Then
            assertThat(actualSummaries).isNotNull();
            assertThat(actualSummaries.size()).isEqualTo(2);
            assertThat(actualSummaries)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedSummaries);
        }

        @Test
        void returnsEmptyList_whenNoStaffAccountsExist() {
            // Given
            when(readJdbcHelperMock.query(any(SqlStatement.class), any()))
                    .thenReturn(List.of());

            // When
            var result = sut.findAllSummaries();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(readJdbcHelperMock.query(any(SqlStatement.class), any()))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.findAllSummaries())
                    .isInstanceOf(InfraException.class);
        }
    }
}