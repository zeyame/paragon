package com.paragon.infrastructure.persistence;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.daos.StaffAccountDao;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.SqlStatement;
import com.paragon.infrastructure.persistence.repos.StaffAccountWriteRepoImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StaffAccountWriteRepoTests {
    @Nested
    class Create {
        private final WriteJdbcHelper jdbcHelperMock;
        private final StaffAccountWriteRepoImpl sut;

        public Create() {
            this.jdbcHelperMock = mock(WriteJdbcHelper.class);
            this.sut = new StaffAccountWriteRepoImpl(jdbcHelperMock);
        }

        @Test
        void callsJdbcHelper_withCorrectInsertStaffAccountSqlStatementAndParams() {
            // Given
            var account = StaffAccountFixture.validStaffAccount();
            ArgumentCaptor<List<SqlStatement>> captor = ArgumentCaptor.forClass(List.class);

            // When
            sut.create(account);
            verify(jdbcHelperMock, times(1)).executeMultiple(captor.capture());
            List<SqlStatement> queries = captor.getValue();

            SqlStatement insertQuery = queries.getFirst();
            var params = insertQuery.params().build();

            assertThat(insertQuery.sql()).contains("INSERT INTO staff_accounts");
            assertThat(params.get("id")).isEqualTo(account.getId().getValue());
            assertThat(params.get("username")).isEqualTo(account.getUsername().getValue());
            assertThat(params.get("password")).isEqualTo(account.getPassword().getValue());
            assertThat(params.get("isPasswordTemporary")).isEqualTo(account.isPasswordTemporary());
            assertThat(params.get("status")).isEqualTo(account.getStatus().toString());
        }

        @Test
        void callsJdbcHelper_withExpectedNumberOfQueries() {
            // Given
            var account = StaffAccountFixture.validStaffAccount();
            ArgumentCaptor<List<SqlStatement>> captor = ArgumentCaptor.forClass(List.class);

            // When
            sut.create(account);

            // Then
            verify(jdbcHelperMock, times(1)).executeMultiple(captor.capture());
            List<SqlStatement> queries = captor.getValue();

            assertThat(queries.size()).isEqualTo(account.getPermissionCodes().size() + 1); // 1 for account insertion + N for permission ids
        }

        @Test
        void insertsJoinTableEntries_forEachPermission() {
            // Given
            var account = StaffAccountFixture.validStaffAccount();
            ArgumentCaptor<List<SqlStatement>> captor = ArgumentCaptor.forClass(List.class);

            // When
            sut.create(account);

            // Then
            verify(jdbcHelperMock, times(1)).executeMultiple(captor.capture());
            List<SqlStatement> queries = captor.getValue();

            List<PermissionCode> permissionCodes = account
                    .getPermissionCodes()
                    .stream()
                    .toList();

            for (PermissionCode permissionCode : permissionCodes) {
                assertThat(queries)
                        .anySatisfy(q -> {
                            assertThat(q.sql()).contains(("INSERT INTO staff_account_permissions"));
                            assertThat(q.params().build().get("staffAccountId")).isEqualTo(account.getId().getValue());
                            assertThat(q.params().build().get("permissionCode")).isEqualTo(permissionCode.getValue());
                        });
            }
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            doThrow(InfraException.class)
                    .when(jdbcHelperMock)
                    .executeMultiple(anyList());

            // When & Then
            assertThatThrownBy(() -> sut.create(StaffAccountFixture.validStaffAccount()))
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class GetById {
        private final WriteJdbcHelper jdbcHelper;
        private final StaffAccountWriteRepoImpl sut;

        public GetById() {
            this.jdbcHelper = mock(WriteJdbcHelper.class);
            this.sut = new StaffAccountWriteRepoImpl(jdbcHelper);
        }

        @Test
        void callsJdbcHelper_withExpectedSqlAndParams() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<SqlParamsBuilder> paramsCaptor = ArgumentCaptor.forClass(SqlParamsBuilder.class);

            // When
            sut.getById(staffAccountId);

            // Then
            verify(jdbcHelper, times(1)).queryFirstOrDefault(sqlCaptor.capture(), paramsCaptor.capture(), eq(StaffAccountDao.class));

            String sql = sqlCaptor.getValue();
            SqlParamsBuilder sqlParams = paramsCaptor.getValue();

            assertThat(sql).isEqualTo("SELECT * FROM staff_accounts WHERE id = :id");
            assertThat(sqlParams.build().get("id")).isEqualTo(staffAccountId.getValue());
        }

        @Test
        void returnsMappedStaffAccount_whenStaffAccountExists() {
            // Given
            StaffAccountDao staffAccountDao = createStaffAccountDao();
            StaffAccountId staffAccountId = StaffAccountId.from(staffAccountDao.id().toString());

            when(jdbcHelper.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(StaffAccountDao.class)))
                    .thenReturn(Optional.of(staffAccountDao));

            // When
            Optional<StaffAccount> result = sut.getById(staffAccountId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(staffAccountId);
        }

        @Test
        void returnsEmptyOptional_whenStaffAccountIsMissing() {
            // Given
            when(jdbcHelper.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(StaffAccountDao.class)))
                    .thenReturn(Optional.empty());

            // When
            Optional<StaffAccount> result = sut.getById(StaffAccountId.generate());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(jdbcHelper.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(StaffAccountDao.class)))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.getById(StaffAccountId.generate()))
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class GetByUsername {
        private final WriteJdbcHelper jdbcHelper;
        private final StaffAccountWriteRepoImpl sut;

        public GetByUsername() {
            this.jdbcHelper = mock(WriteJdbcHelper.class);
            this.sut = new StaffAccountWriteRepoImpl(jdbcHelper);
        }

        @Test
        void callsJdbcHelper_withExpectedSqlAndParams() {
            // Given
            Username username = Username.of("john_doe");
            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<SqlParamsBuilder> paramsCaptor = ArgumentCaptor.forClass(SqlParamsBuilder.class);

            // When
            sut.getByUsername(username);

            // Then
            verify(jdbcHelper, times(1)).queryFirstOrDefault(sqlCaptor.capture(), paramsCaptor.capture(), eq(StaffAccountDao.class));

            String sql = sqlCaptor.getValue();
            SqlParamsBuilder sqlParams = paramsCaptor.getValue();

            assertThat(sql).isEqualTo("SELECT * FROM staff_accounts WHERE username = :username");
            assertThat(sqlParams.build().get("username")).isEqualTo(username.getValue());
        }

        @Test
        void returnsMappedStaffAccount_whenStaffAccountExists() {
            // Given
            StaffAccountDao staffAccountDao = createStaffAccountDao();
            Username username = Username.of(staffAccountDao.username());

            when(jdbcHelper.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(StaffAccountDao.class)))
                    .thenReturn(Optional.of(staffAccountDao));

            // When
            Optional<StaffAccount> optionalStaffAccount = sut.getByUsername(username);

            // Then
            assertThat(optionalStaffAccount).isPresent();
            assertThat(optionalStaffAccount.get().getUsername()).isEqualTo(username);
        }

        @Test
        void returnsEmptyOptional_whenStaffAccountIsMissing() {
            // Given
            when(jdbcHelper.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(StaffAccountDao.class)))
                    .thenReturn(Optional.empty());

            // When
            Optional<StaffAccount> result = sut.getByUsername(Username.of("john_doe"));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(jdbcHelper.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(StaffAccountDao.class)))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.getByUsername(Username.of("john_doe")))
                    .isInstanceOf(InfraException.class);
        }
    }

    private StaffAccountDao createStaffAccountDao() {
        return new StaffAccountDao(
                UUID.randomUUID(),
                "testuser",
                "testuser@example.com",
                "SecurePass123!",
                true,
                Instant.parse("2024-01-01T12:00:00Z"),
                7,
                14,
                StaffAccountStatus.PENDING_PASSWORD_CHANGE.name(),
                0,
                null,
                Instant.parse("2024-01-03T12:00:00Z"),
                UUID.randomUUID(),
                null,
                1,
                Instant.parse("2024-01-02T12:00:00Z"),
                Instant.parse("2024-01-03T12:00:00Z")

        );
    }
}
