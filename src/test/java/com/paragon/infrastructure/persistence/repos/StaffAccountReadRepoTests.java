package com.paragon.infrastructure.persistence.repos;

import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.valueobjects.DateTimeUtc;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.infrastructure.persistence.daos.PermissionCodeDao;
import com.paragon.infrastructure.persistence.daos.StaffAccountDetailedReadModelDao;
import com.paragon.infrastructure.persistence.daos.StaffAccountIdDao;
import com.paragon.infrastructure.persistence.daos.StaffAccountStatusDao;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.helpers.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import com.paragon.infrastructure.persistence.readmodels.StaffAccountDetailedReadModel;
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
            UUID staffAccountId = UUID.randomUUID();
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountIdDao.class)))
                    .thenReturn(Optional.empty());

            // When
            sut.exists(staffAccountId);

            // Then
            verify(readJdbcHelperMock, times(1)).queryFirstOrDefault(sqlStatementCaptor.capture(), eq(StaffAccountIdDao.class));

            SqlStatement statement = sqlStatementCaptor.getValue();

            assertThat(statement.sql()).isEqualTo("SELECT id FROM staff_accounts WHERE id = :id");
            assertThat(statement.params().build().get("id")).isEqualTo(staffAccountId);
        }

        @Test
        void returnsTrue_whenStaffAccountExists() {
            // Given
            UUID staffAccountId = UUID.randomUUID();
            StaffAccountIdDao dao = new StaffAccountIdDao(staffAccountId);
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
            UUID staffAccountId = UUID.randomUUID();
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
            UUID staffAccountId = UUID.randomUUID();
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
            UUID staffAccountId = UUID.randomUUID();
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
            assertThat(statement.params().build().get("staffAccountId")).isEqualTo(staffAccountId);
            assertThat(statement.params().build().get("permissionCode")).isEqualTo(permissionCode.getValue());
        }

        @Test
        void returnsTrue_whenStaffAccountHasPermission() {
            // Given
            UUID staffAccountId = UUID.randomUUID();
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
            UUID staffAccountId = UUID.randomUUID();
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
            UUID staffAccountId = UUID.randomUUID();
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), any()))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.hasPermission(staffAccountId, permissionCode))
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
            String expectedSql = """
                SELECT id, username, status, created_at_utc
                FROM staff_accounts
                WHERE username = :username
                """;
            SqlParamsBuilder expectedParams = new SqlParamsBuilder()
                    .add("username", "john_doe");

            // When
            sut.findSummaryByUsername("john_doe");

            // Then
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);
            verify(readJdbcHelperMock, times(1))
                    .queryFirstOrDefault(sqlStatementCaptor.capture(), eq(StaffAccountSummaryReadModel.class));

            SqlStatement capturedSqlStatement = sqlStatementCaptor.getValue();
            assertThat(capturedSqlStatement.sql()).isEqualTo(expectedSql);
            assertThat(capturedSqlStatement.params().build().get("username")).isEqualTo("john_doe");
        }

        @Test
        void shouldReturnExpectedSummaryModel() {
            // Given
            StaffAccountSummaryReadModel expectedStaffAccountSummary = new StaffAccountSummaryReadModel(
                    UUID.randomUUID(),
                    "john_doe",
                    "active",
                    Instant.now()
            );

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(Optional.of(expectedStaffAccountSummary));

            // When
            Optional<StaffAccountSummaryReadModel> optionalStaffAccountSummary = sut.findSummaryByUsername("john_doe");

            // Then
            assertThat(optionalStaffAccountSummary).isPresent();
            assertThat(optionalStaffAccountSummary.get()).isEqualTo(expectedStaffAccountSummary);
        }

        @Test
        void returnsEmptyOptional_whenStaffAccountDoesNotExist() {
            // Given
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(Optional.empty());

            // When
            Optional<StaffAccountSummaryReadModel> optionalStaffAccountSummary = sut.findSummaryByUsername("john_doe");

            // Then
            assertThat(optionalStaffAccountSummary).isEmpty();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.findSummaryByUsername("john_doe"))
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
                Username enabledBy,
                Username disabledBy,
                DateTimeUtc createdBefore,
                DateTimeUtc createdAfter
        ) {
            // Given
            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(List.of());

            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            // When
            sut.findAllSummaries(status, enabledBy, disabledBy, createdBefore, createdAfter);

            // Then
            verify(readJdbcHelperMock, times(1)).query(sqlStatementCaptor.capture(), eq(StaffAccountSummaryReadModel.class));

            SqlStatement statement = sqlStatementCaptor.getValue();
            String actualSql = statement.sql();
            var params = statement.params().build();

            // Verify base SELECT and WHERE 1=1
            assertThat(actualSql).contains("SELECT id, username, status, created_at_utc");
            assertThat(actualSql).contains("FROM staff_accounts");
            assertThat(actualSql).contains("WHERE 1=1");
            assertThat(actualSql).endsWith("ORDER BY created_at_utc DESC");

            // Verify filters
            if (status != null) {
                assertThat(actualSql).contains("AND status = :status");
                assertThat(params.get("status")).isEqualTo(status.name());
            }

            if (enabledBy != null) {
                assertThat(actualSql).contains("AND EXISTS (SELECT 1 FROM staff_accounts enabler WHERE enabler.id = staff_accounts.enabled_by AND enabler.username = :enabledBy)");
                assertThat(params.get("enabledBy")).isEqualTo(enabledBy.getValue());
            }

            if (disabledBy != null) {
                assertThat(actualSql).contains("AND EXISTS (SELECT 1 FROM staff_accounts disabler WHERE disabler.id = staff_accounts.disabled_by AND disabler.username = :disabledBy)");
                assertThat(params.get("disabledBy")).isEqualTo(disabledBy.getValue());
            }

            if (createdBefore != null) {
                assertThat(actualSql).contains("AND created_at_utc < :createdBefore");
                assertThat(params.get("createdBefore")).isEqualTo(Timestamp.from(createdBefore.getValue()));
            }

            if (createdAfter != null) {
                assertThat(actualSql).contains("AND created_at_utc > :createdAfter");
                assertThat(params.get("createdAfter")).isEqualTo(Timestamp.from(createdAfter.getValue()));
            }
        }

        @Test
        void shouldReturnEmptyList_whenNoFiltersAndNoResults() {
            // Given
            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(List.of());

            // When
            var result = sut.findAllSummaries(null, null, null, null, null);

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
                    Instant.now()
            );

            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenReturn(List.of(summary));

            // When
            var result = sut.findAllSummaries(StaffAccountStatus.ACTIVE, null, null, null, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(summary);
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(StaffAccountSummaryReadModel.class)))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.findAllSummaries(null, null, null, null, null))
                    .isInstanceOf(InfraException.class);
        }

        private static Stream<Arguments> provideFilterCombinations() {
            Username enabledByUsername = Username.of("enabler_user");
            Username disabledByUsername = Username.of("disabler_user");
            DateTimeUtc beforeInstant = DateTimeUtc.from("2024-12-31T23:59:59Z");
            DateTimeUtc afterInstant = DateTimeUtc.from("2024-01-01T00:00:00Z");

            return Stream.of(
                    // No filters
                    Arguments.of(null, null, null, null, null),

                    // Single filter: status
                    Arguments.of(StaffAccountStatus.ACTIVE, null, null, null, null),
                    Arguments.of(StaffAccountStatus.DISABLED, null, null, null, null),

                    // Single filter: enabledBy
                    Arguments.of(null, enabledByUsername, null, null, null),

                    // Single filter: disabledBy
                    Arguments.of(null, null, disabledByUsername, null, null),

                    // Single filter: createdBefore
                    Arguments.of(null, null, null, beforeInstant, null),

                    // Single filter: createdAfter
                    Arguments.of(null, null, null, null, afterInstant),

                    // Two filters: status + enabledBy
                    Arguments.of(StaffAccountStatus.ACTIVE, enabledByUsername, null, null, null),

                    // Two filters: status + disabledBy
                    Arguments.of(StaffAccountStatus.DISABLED, null, disabledByUsername, null, null),

                    // Two filters: status + createdBefore
                    Arguments.of(StaffAccountStatus.ACTIVE, null, null, beforeInstant, null),

                    // Two filters: status + createdAfter
                    Arguments.of(StaffAccountStatus.ACTIVE, null, null, null, afterInstant),

                    // Two filters: enabledBy + createdBefore
                    Arguments.of(null, enabledByUsername, null, beforeInstant, null),

                    // Two filters: disabledBy + createdAfter
                    Arguments.of(null, null, disabledByUsername, null, afterInstant),

                    // Two filters: date range
                    Arguments.of(null, null, null, beforeInstant, afterInstant),

                    // Three filters: status + enabledBy + createdBefore
                    Arguments.of(StaffAccountStatus.ACTIVE, enabledByUsername, null, beforeInstant, null),

                    // Three filters: status + date range
                    Arguments.of(StaffAccountStatus.ACTIVE, null, null, beforeInstant, afterInstant),

                    // Four filters: status + disabledBy + date range
                    Arguments.of(StaffAccountStatus.DISABLED, null, disabledByUsername, beforeInstant, afterInstant),

                    // All five filters (status, enabledBy, createdBefore, createdAfter - note: can't have both enabledBy and disabledBy)
                    Arguments.of(StaffAccountStatus.ACTIVE, enabledByUsername, null, beforeInstant, afterInstant)
            );
        }
    }

    @Nested
    class FindDetailedById {

        private final ReadJdbcHelper readJdbcHelperMock;
        private final StaffAccountReadRepo sut;

        public FindDetailedById() {
            this.readJdbcHelperMock = mock(ReadJdbcHelper.class);
            this.sut = new StaffAccountReadRepoImpl(readJdbcHelperMock);
        }

        @Test
        void shouldExecuteQueryWithCorrectSqlAndParams() {
            // Given
            UUID staffAccountId = UUID.randomUUID();

            String expectedSql = """
                SELECT
                    id,
                    username,
                    order_access_duration AS order_access_duration_in_days,
                    modmail_transcript_access_duration AS modmail_transcript_access_duration_in_days,
                    status,
                    locked_until_utc,
                    last_login_at_utc,
                    created_by,
                    disabled_by,
                    created_at_utc
                FROM staff_accounts
                WHERE id = :id
                """;

            // Return a DAO so the method continues to the permission query
            StaffAccountDetailedReadModelDao dao = new StaffAccountDetailedReadModelDao(
                    staffAccountId,
                    "john_doe",
                    10,
                    20,
                    "ACTIVE",
                    Instant.now(),
                    Instant.now(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.now()
            );

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountDetailedReadModelDao.class)))
                    .thenReturn(Optional.of(dao));

            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(PermissionCodeDao.class)))
                    .thenReturn(List.of());

            // When
            sut.findDetailedById(staffAccountId);

            // Then
            ArgumentCaptor<SqlStatement> captor = ArgumentCaptor.forClass(SqlStatement.class);
            verify(readJdbcHelperMock).queryFirstOrDefault(captor.capture(), eq(StaffAccountDetailedReadModelDao.class));

            SqlStatement sqlStatement = captor.getValue();

            assertThat(sqlStatement.sql()).isEqualToIgnoringWhitespace(expectedSql);
            assertThat(sqlStatement.params().get("id")).isEqualTo(staffAccountId);
        }

        @Test
        void shouldReturnExpectedDetailedModel() {
            // Given
            UUID staffAccountId = UUID.randomUUID();

            StaffAccountDetailedReadModelDao dao = new StaffAccountDetailedReadModelDao(
                    staffAccountId,
                    "john_doe",
                    14,
                    30,
                    "LOCKED",
                    Instant.parse("2025-01-01T12:00:00Z"),
                    Instant.parse("2024-12-31T23:45:00Z"),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.parse("2023-11-15T08:30:00Z")
            );

            List<PermissionCodeDao> permDaos = List.of(
                    new PermissionCodeDao("MANAGE_ACCOUNTS"),
                    new PermissionCodeDao("VIEW_ACCOUNTS_LIST")
            );

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountDetailedReadModelDao.class)))
                    .thenReturn(Optional.of(dao));
            when(readJdbcHelperMock.query(any(SqlStatement.class), eq(PermissionCodeDao.class)))
                    .thenReturn(permDaos);

            // Expected final result
            StaffAccountDetailedReadModel expected = StaffAccountDetailedReadModel.from(
                    dao,
                    List.of("MANAGE_ACCOUNTS", "VIEW_ACCOUNTS_LIST")
            );

            // When
            Optional<StaffAccountDetailedReadModel> result = sut.findDetailedById(staffAccountId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expected);
        }

        @Test
        void returnsEmptyOptional_whenStaffAccountDoesNotExist() {
            // Given
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountDetailedReadModelDao.class)))
                    .thenReturn(Optional.empty());

            // When
            Optional<StaffAccountDetailedReadModel> result = sut.findDetailedById(UUID.randomUUID());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountDetailedReadModelDao.class)))
                    .thenThrow(new InfraException());

            // When & Then
            assertThatThrownBy(() -> sut.findDetailedById(UUID.randomUUID()))
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class FindStatusById {
        private final StaffAccountReadRepo sut;
        private final ReadJdbcHelper readJdbcHelperMock;

        public FindStatusById() {
            readJdbcHelperMock = mock(ReadJdbcHelper.class);
            sut = new StaffAccountReadRepoImpl(readJdbcHelperMock);
        }

        @Test
        void shouldExecuteQueryWithCorrectSqlAndParams() {
            // Given
            UUID staffAccountId = UUID.randomUUID();
            String expectedSql = """
                        SELECT status FROM staff_accounts
                        WHERE id = :id
                    """;

            // When
            sut.findStatusById(staffAccountId);

            // Then
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);
            verify(readJdbcHelperMock, times(1)).queryFirstOrDefault(sqlStatementCaptor.capture(), eq(StaffAccountStatusDao.class));

            SqlStatement sqlStatement = sqlStatementCaptor.getValue();
            assertThat(sqlStatement.sql()).isEqualTo(expectedSql);
            assertThat(sqlStatement.params().get("id")).isEqualTo(staffAccountId);
        }

        @Test
        void shouldReturnExpectedStaffAccountStatus() {
            // Given
            UUID staffAccountId = UUID.randomUUID();

            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountStatusDao.class)))
                    .thenReturn(Optional.of(new StaffAccountStatusDao("ACTIVE")));

            // When
            Optional<StaffAccountStatus> optionalStaffAccountStatus = sut.findStatusById(staffAccountId);

            // Then
            assertThat(optionalStaffAccountStatus).isPresent();
            assertThat(optionalStaffAccountStatus.get()).isEqualTo(StaffAccountStatus.ACTIVE);
        }

        @Test
        void returnsEmptyOptional_whenStaffAccountDoesNotExist() {
            // Given
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountStatusDao.class)))
                    .thenReturn(Optional.empty());

            // When
            Optional<StaffAccountStatus> optionalStaffAccountStatus = sut.findStatusById(UUID.randomUUID());

            // Then
            assertThat(optionalStaffAccountStatus).isEmpty();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(readJdbcHelperMock.queryFirstOrDefault(any(SqlStatement.class), eq(StaffAccountStatusDao.class)))
                    .thenThrow(new InfraException());

            // When & Then
            assertThatThrownBy(() -> sut.findStatusById(UUID.randomUUID()))
                    .isInstanceOf(InfraException.class);
        }
    }
}
