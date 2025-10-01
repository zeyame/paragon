package com.paragon.infrastructure.persistence;

import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.*;
import com.paragon.infrastructure.persistence.daos.StaffAccountDao;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.SqlParams;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.WriteQuery;
import com.paragon.infrastructure.persistence.repos.StaffAccountWriteRepoImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        void callsJdbcHelper_withExpectedNumberOfQueries() {
            // Given
            var account = createStaffAccount();
            ArgumentCaptor<List<WriteQuery>> captor = ArgumentCaptor.forClass(List.class);

            // When
            sut.create(account);

            // Then
            verify(jdbcHelperMock, times(1)).executeMultiple(captor.capture());
            List<WriteQuery> queries = captor.getValue();

            assertThat(queries.size()).isEqualTo(account.getPermissionIds().size() + 1); // 1 for account insertion + N for permission ids

            WriteQuery insertQuery = queries.getFirst();
            assertThat(insertQuery.sql()).contains("INSERT INTO staff_accounts");
            assertThat(insertQuery.params().build().get("id")).isEqualTo(account.getId().getValue());
            assertThat(insertQuery.params().build().get("username")).isEqualTo(account.getUsername().getValue());
            assertThat(insertQuery.params().build().get("password")).isEqualTo(account.getPassword().getValue());
            assertThat(insertQuery.params().build().get("status")).isEqualTo(account.getStatus().toString());
        }

        @Test
        void insertsJoinTableEntries_forEachPermission() {
            // Given
            var account = createStaffAccount();
            ArgumentCaptor<List<WriteQuery>> captor = ArgumentCaptor.forClass(List.class);

            // When
            sut.create(account);

            // Then
            verify(jdbcHelperMock, times(1)).executeMultiple(captor.capture());
            List<WriteQuery> queries = captor.getValue();

            List<PermissionId> permissionIds = account
                    .getPermissionIds()
                    .stream()
                    .toList();

            for (PermissionId permissionId : permissionIds) {
                assertThat(queries)
                        .anySatisfy(q -> {
                            assertThat(q.sql()).contains(("INSERT INTO staff_account_permissions"));
                            assertThat(q.params().build().get("staffAccountId")).isEqualTo(account.getId().getValue());
                            assertThat(q.params().build().get("permissionId")).isEqualTo(permissionId.getValue());
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
            assertThatThrownBy(() -> sut.create(createStaffAccount()))
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
            ArgumentCaptor<SqlParams> paramsCaptor = ArgumentCaptor.forClass(SqlParams.class);

            // When
            sut.getById(staffAccountId);

            // Then
            verify(jdbcHelper, times(1)).queryFirstOrDefault(sqlCaptor.capture(), paramsCaptor.capture(), eq(StaffAccountDao.class));

            String sql = sqlCaptor.getValue();
            SqlParams sqlParams = paramsCaptor.getValue();

            assertThat(sql).isEqualTo("SELECT * FROM staff_accounts WHERE id = :id");
            assertThat(sqlParams.build().get("id")).isEqualTo(staffAccountId.getValue());
        }

        @Test
        void returnsMappedStaffAccount_whenStaffAccountExists() {
            // Given
            StaffAccountDao staffAccountDao = createStaffAccountDao();
            StaffAccountId staffAccountId = StaffAccountId.from(staffAccountDao.id().toString());

            when(jdbcHelper.queryFirstOrDefault(anyString(), any(SqlParams.class), eq(StaffAccountDao.class)))
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
            when(jdbcHelper.queryFirstOrDefault(anyString(), any(SqlParams.class), eq(StaffAccountDao.class)))
                    .thenReturn(Optional.empty());

            // When
            Optional<StaffAccount> result = sut.getById(StaffAccountId.generate());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            when(jdbcHelper.queryFirstOrDefault(anyString(), any(SqlParams.class), eq(StaffAccountDao.class)))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.getById(StaffAccountId.generate()))
                    .isInstanceOf(InfraException.class);
        }
    }

    private StaffAccount createStaffAccount() {
        return StaffAccount.createFrom(
                StaffAccountId.of(UUID.randomUUID()),
                Username.of("testuser"),
                Email.of("testuser@example.com"),
                Password.of("SecurePass123!"),
                Instant.parse("2024-01-01T12:00:00Z"),
                OrderAccessDuration.from(7),
                ModmailTranscriptAccessDuration.from(14),
                StaffAccountStatus.PENDING_PASSWORD_CHANGE,
                FailedLoginAttempts.of(2),
                Instant.parse("2024-01-02T12:00:00Z"),
                Instant.parse("2024-01-03T12:00:00Z"),
                StaffAccountId.of(UUID.randomUUID()),
                null,
                Set.of(PermissionId.of(UUID.randomUUID()), PermissionId.of(UUID.randomUUID())),
                Version.of(1)
        );
    }

    private StaffAccountDao createStaffAccountDao() {
        return new StaffAccountDao(
                UUID.randomUUID(),
                "testuser",
                "testuser@example.com",
                "SecurePass123!",
                Instant.parse("2024-01-01T12:00:00Z"),
                7,
                14,
                StaffAccountStatus.PENDING_PASSWORD_CHANGE.name(),
                2,
                Instant.parse("2024-01-02T12:00:00Z"),
                Instant.parse("2024-01-03T12:00:00Z"),
                UUID.randomUUID(),
                null,
                List.of(UUID.randomUUID(), UUID.randomUUID()),
                1,
                Instant.parse("2024-01-02T12:00:00Z"),
                Instant.parse("2024-01-03T12:00:00Z")

        );
    }
}
