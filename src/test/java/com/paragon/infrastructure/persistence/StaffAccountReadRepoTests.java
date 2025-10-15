package com.paragon.infrastructure.persistence;

import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.ReadJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.repos.StaffAccountReadRepoImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<SqlParamsBuilder> paramsCaptor = ArgumentCaptor.forClass(SqlParamsBuilder.class);

            when(readJdbcHelperMock.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(UUID.class)))
                    .thenReturn(Optional.empty());

            // When
            sut.exists(staffAccountId);

            // Then
            verify(readJdbcHelperMock, times(1)).queryFirstOrDefault(sqlCaptor.capture(), paramsCaptor.capture(), eq(UUID.class));

            String sql = sqlCaptor.getValue();
            SqlParamsBuilder sqlParams = paramsCaptor.getValue();

            assertThat(sql).isEqualTo("SELECT id FROM staff_accounts WHERE id = :id");
            assertThat(sqlParams.build().get("id")).isEqualTo(staffAccountId.getValue());
        }

        @Test
        void returnsTrue_whenStaffAccountExists() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            when(readJdbcHelperMock.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(UUID.class)))
                    .thenReturn(Optional.of(staffAccountId.getValue()));

            // When
            boolean result = sut.exists(staffAccountId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void returnsFalse_whenStaffAccountDoesNotExist() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            when(readJdbcHelperMock.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(UUID.class)))
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
            when(readJdbcHelperMock.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), eq(UUID.class)))
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
            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<SqlParamsBuilder> paramsCaptor = ArgumentCaptor.forClass(SqlParamsBuilder.class);

            when(readJdbcHelperMock.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), any()))
                    .thenReturn(Optional.empty());

            // When
            sut.hasPermission(staffAccountId, permissionCode);

            // Then
            verify(readJdbcHelperMock, times(1)).queryFirstOrDefault(sqlCaptor.capture(), paramsCaptor.capture(), any());

            String sql = sqlCaptor.getValue();
            SqlParamsBuilder sqlParams = paramsCaptor.getValue();

            assertThat(sql).isEqualTo("SELECT * FROM staff_account_permissions WHERE staff_account_id = :staffAccountId AND permission_code = :permissionCode");
            assertThat(sqlParams.build().get("staffAccountId")).isEqualTo(staffAccountId.getValue());
            assertThat(sqlParams.build().get("permissionCode")).isEqualTo(permissionCode.getValue());
        }

        @Test
        void returnsTrue_whenStaffAccountHasPermission() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            PermissionCode permissionCode = PermissionCode.of("VIEW_ACCOUNTS_LIST");

            when(readJdbcHelperMock.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), any()))
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

            when(readJdbcHelperMock.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), any()))
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

            when(readJdbcHelperMock.queryFirstOrDefault(anyString(), any(SqlParamsBuilder.class), any()))
                    .thenThrow(InfraException.class);

            // When & Then
            assertThatThrownBy(() -> sut.hasPermission(staffAccountId, permissionCode))
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class FindAllSummaries {

    }
}
