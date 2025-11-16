package com.paragon.infrastructure.persistence.repos;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.daos.StaffAccountIdDao;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.helpers.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountSummaryReadModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class StaffAccountReadRepoTests {
    @Nested
    class Exists {
        private final ReadJdbcHelper readJdbcHelperMock;
        private final StaffAccountReadRepo sut;

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
        private final StaffAccountReadRepo sut;

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
        private final StaffAccountReadRepo sut;

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
            sut.findAll();

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
                    Instant.now()
            );
            StaffAccountSummaryReadModel summary2 = new StaffAccountSummaryReadModel(
                    UUID.randomUUID(),
                    "jane_smith",
                    "pending_password_change",
                    14,
                    7,
                    Instant.now()
            );
            List<StaffAccountSummaryReadModel> expectedSummaries = List.of(summary1, summary2);

            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(expectedSummaries);

            // When
            var actualSummaries = sut.findAll();

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
            var result = sut.findAll();

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
            assertThatThrownBy(() -> sut.findAll())
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class FindByUsername {
        private final ReadJdbcHelper readJdbcHelperMock;
        private final StaffAccountReadRepo sut;

        public FindByUsername() {
            this.readJdbcHelperMock = mock(ReadJdbcHelper.class);
            this.sut = new StaffAccountReadRepoImpl(readJdbcHelperMock);
        }

        @Test
        void callsJdbcHelperWithExpectedSqlAndParams() {
            // Given
            Username username = Username.of("john_doe");
            String expectedSql = """
                SELECT id, username, status, order_access_duration, modmail_transcript_access_duration, created_at_utc
                FROM staff_accounts
                WHERE username = :username
                """;
            SqlParamsBuilder expectedParams = new SqlParamsBuilder()
                    .add("username", username.getValue());

            // When
            sut.findByUsername(username);

            // Then
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);
            verify(readJdbcHelperMock, times(1))
                    .queryFirstOrDefault(sqlStatementCaptor.capture(), eq(StaffAccountSummaryReadModel.class));

            SqlStatement capturedSqlStatement = sqlStatementCaptor.getValue();
            assertThat(capturedSqlStatement.sql()).isEqualTo(expectedSql);
            assertThat(capturedSqlStatement.params().build().get("username")).isEqualTo(expectedParams.build().get("username"));
        }

        @Test
        void returnsExpectedSummaryModel_whenStaffAccountExists() {
            // Given
            Username username = Username.of("john_doe");
            StaffAccountSummaryReadModel expectedStaffAccountSummary = new StaffAccountSummaryReadModel(
                    UUID.randomUUID(),
                    "john_doe",
                    "active",
                    10,
                    5,
                    Instant.now()
            );

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(Optional.of(expectedStaffAccountSummary));

            // When
            Optional<StaffAccountSummaryReadModel> optionalStaffAccountSummary = sut.findByUsername(username);

            // Then
            assertThat(optionalStaffAccountSummary).isPresent();
            assertThat(optionalStaffAccountSummary.get()).isEqualTo(expectedStaffAccountSummary);
        }

        @Test
        void returnsEmptyOptional_whenStaffAccountDoesNotExist() {
            // Given
            Username username = Username.of("john_doe");

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(Optional.empty());

            // When
            Optional<StaffAccountSummaryReadModel> optionalStaffAccountSummary = sut.findByUsername(username);

            // Then
            assertThat(optionalStaffAccountSummary).isEmpty();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.findByUsername(Username.of("john_doe")))
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class FindAll {
        private final ReadJdbcHelper readJdbcHelperMock;
        private final StaffAccountReadRepo sut;

        public FindAll() {
            this.readJdbcHelperMock = mock(ReadJdbcHelper.class);
            this.sut = new StaffAccountReadRepoImpl(readJdbcHelperMock);
        }

        @ParameterizedTest
        @MethodSource("provideFilterCombinations")
        void shouldBuildCorrectSqlAndParams(
                StaffAccountStatus status,
                StaffAccountId enabledBy,
                StaffAccountId disabledBy,
                Instant createdBefore,
                Instant createdAfter
        ) {
            // Given
            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(List.of());

            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            // When
            sut.findAll(status, enabledBy, disabledBy, createdBefore, createdAfter);

            // Then
            verify(readJdbcHelperMock, times(1)).query(sqlStatementCaptor.capture(), eq(StaffAccountSummaryReadModel.class));

            SqlStatement statement = sqlStatementCaptor.getValue();
            String actualSql = statement.sql();
            var params = statement.params().build();

            // Verify base SELECT and WHERE 1=1
            assertThat(actualSql).contains("SELECT id, username, status, order_access_duration, modmail_transcript_access_duration, created_at_utc");
            assertThat(actualSql).contains("FROM staff_accounts");
            assertThat(actualSql).contains("WHERE 1=1");
            assertThat(actualSql).endsWith("ORDER BY created_at_utc DESC");

            // Verify filters
            if (status != null) {
                assertThat(actualSql).contains("AND status = :status");
                assertThat(params.get("status")).isEqualTo(status.name());
            }

            if (enabledBy != null) {
                assertThat(actualSql).contains("AND enabled_by = :enabledBy");
                assertThat(params.get("enabledBy")).isEqualTo(enabledBy.getValue());
            }

            if (disabledBy != null) {
                assertThat(actualSql).contains("AND disabled_by = :disabledBy");
                assertThat(params.get("disabledBy")).isEqualTo(disabledBy.getValue());
            }

            if (createdBefore != null) {
                assertThat(actualSql).contains("AND created_at_utc < :createdBefore");
                assertThat(params.get("createdBefore")).isEqualTo(Timestamp.from(createdBefore));
            }

            if (createdAfter != null) {
                assertThat(actualSql).contains("AND created_at_utc > :createdAfter");
                assertThat(params.get("createdAfter")).isEqualTo(Timestamp.from(createdAfter));
            }
        }

        @Test
        void shouldReturnEmptyList_whenNoFiltersAndNoResults() {
            // Given
            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(List.of());

            // When
            var result = sut.findAll(null, null, null, null, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnResults_whenFiltersMatch() {
            // Given
            StaffAccountSummaryReadModel summary = new StaffAccountSummaryReadModel(
                    UUID.randomUUID(),
                    "john_doe",
                    "active",
                    10,
                    5,
                    Instant.now()
            );

            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(List.of(summary));

            // When
            var result = sut.findAll(StaffAccountStatus.ACTIVE, null, null, null, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(summary);
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.findAll(null, null, null, null, null))
                    .isInstanceOf(InfraException.class);
        }

        private static Stream<Arguments> provideFilterCombinations() {
            StaffAccountId enabledById = StaffAccountId.generate();
            StaffAccountId disabledById = StaffAccountId.generate();
            Instant beforeInstant = Instant.parse("2024-12-31T23:59:59Z");
            Instant afterInstant = Instant.parse("2024-01-01T00:00:00Z");

            return Stream.of(
                    // No filters
                    Arguments.of(null, null, null, null, null),

                    // Single filter: status
                    Arguments.of(StaffAccountStatus.ACTIVE, null, null, null, null),
                    Arguments.of(StaffAccountStatus.DISABLED, null, null, null, null),

                    // Single filter: enabledBy
                    Arguments.of(null, enabledById, null, null, null),

                    // Single filter: disabledBy
                    Arguments.of(null, null, disabledById, null, null),

                    // Single filter: createdBefore
                    Arguments.of(null, null, null, beforeInstant, null),

                    // Single filter: createdAfter
                    Arguments.of(null, null, null, null, afterInstant),

                    // Two filters: status + enabledBy
                    Arguments.of(StaffAccountStatus.ACTIVE, enabledById, null, null, null),

                    // Two filters: status + disabledBy
                    Arguments.of(StaffAccountStatus.DISABLED, null, disabledById, null, null),

                    // Two filters: status + createdBefore
                    Arguments.of(StaffAccountStatus.ACTIVE, null, null, beforeInstant, null),

                    // Two filters: status + createdAfter
                    Arguments.of(StaffAccountStatus.ACTIVE, null, null, null, afterInstant),

                    // Two filters: enabledBy + createdBefore
                    Arguments.of(null, enabledById, null, beforeInstant, null),

                    // Two filters: disabledBy + createdAfter
                    Arguments.of(null, null, disabledById, null, afterInstant),

                    // Two filters: date range
                    Arguments.of(null, null, null, beforeInstant, afterInstant),

                    // Three filters: status + enabledBy + createdBefore
                    Arguments.of(StaffAccountStatus.ACTIVE, enabledById, null, beforeInstant, null),

                    // Three filters: status + date range
                    Arguments.of(StaffAccountStatus.ACTIVE, null, null, beforeInstant, afterInstant),

                    // Four filters: status + disabledBy + date range
                    Arguments.of(StaffAccountStatus.DISABLED, null, disabledById, beforeInstant, afterInstant),

                    // All five filters (status, enabledBy, createdBefore, createdAfter - note: can't have both enabledBy and disabledBy)
                    Arguments.of(StaffAccountStatus.ACTIVE, enabledById, null, beforeInstant, afterInstant)
            );
        }
    }
}