package com.paragon.infrastructure.persistence;

import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.RefreshTokenDaoFixture;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.infrastructure.persistence.daos.RefreshTokenDao;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.SqlStatement;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.RefreshTokenWriteRepoImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RefreshTokenWriteRepoTests {
    @Nested
    class Create {
        private final WriteJdbcHelper jdbcHelperMock;
        private RefreshTokenWriteRepoImpl sut;

        public Create() {
            jdbcHelperMock = mock(WriteJdbcHelper.class);
            sut = new RefreshTokenWriteRepoImpl(jdbcHelperMock);
        }

        @Test
        void shouldCallJdbcHelperWithCorrectSqlAndParams() {
            // Given
            RefreshToken refreshToken = RefreshTokenFixture.validRefreshToken();

            String expectedSql = """
                    INSERT INTO refresh_tokens
                    (id, staff_account_id, token_hash, issued_from_ip_address, expires_at_utc, is_revoked,
                     revoked_at_utc, replaced_by, version, created_at_utc, updated_at_utc)
                    VALUES
                    (:id, :staffAccountId, :tokenHash, :issuedFromIpAddress, :expiresAtUtc, :isRevoked,
                     :revokedAtUtc, :replacedBy, :version, :createdAtUtc, :updatedAtUtc
                    """;

            // When
            sut.create(refreshToken);

            // Then
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            verify(jdbcHelperMock, times(1))
                    .execute(sqlStatementCaptor.capture());

            SqlStatement actualStatement = sqlStatementCaptor.getValue();

            assertThat(actualStatement.sql()).isEqualTo(expectedSql);
            assertThat(actualStatement.params().build().get("id")).isEqualTo(refreshToken.getId().getValue());
            assertThat(actualStatement.params().build().get("staffAccountId")).isEqualTo(refreshToken.getStaffAccountId().getValue());
            assertThat(actualStatement.params().build().get("tokenHash")).isEqualTo(refreshToken.getTokenHash().getValue());
            assertThat(actualStatement.params().build().get("issuedFromIpAddress")).isEqualTo(refreshToken.getIssuedFromIpAddress().getValue());
            assertThat(actualStatement.params().build().get("expiresAtUtc")).isNotNull();
            assertThat(actualStatement.params().build().get("isRevoked")).isEqualTo(refreshToken.isRevoked());
            assertThat(actualStatement.params().build().get("revokedAtUtc")).isNull();
            assertThat(actualStatement.params().build().get("replacedBy")).isEqualTo(refreshToken.getReplacedBy() != null ? refreshToken.getReplacedBy().getValue() : null);
            assertThat(actualStatement.params().build().get("version")).isEqualTo(refreshToken.getVersion().getValue());
            assertThat(actualStatement.params().build().get("createdAtUtc")).isNotNull();
            assertThat(actualStatement.params().build().get("updatedAtUtc")).isNotNull();
        }

        @Test
        void shouldPropagateInfraException() {
            // Given
            RefreshToken refreshToken = RefreshTokenFixture.validRefreshToken();

            doThrow(InfraException.class)
                    .when(jdbcHelperMock)
                    .execute(any(SqlStatement.class));

            // When & Then
            assertThatExceptionOfType(InfraException.class)
                    .isThrownBy(() -> sut.create(refreshToken));
        }
    }

    @Nested
    class GetActiveTokensByStaffAccountId {
        private final WriteJdbcHelper jdbcHelperMock;
        private final RefreshTokenWriteRepoImpl sut;

        public GetActiveTokensByStaffAccountId() {
            jdbcHelperMock = mock(WriteJdbcHelper.class);
            sut = new RefreshTokenWriteRepoImpl(jdbcHelperMock);
        }

        @Test
        void shouldCallJdbcHelperWithCorrectSqlAndParams() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            String expectedSql = """
                        SELECT * FROM refresh_tokens
                        WHERE staff_account_id = :staffAccountId
                        AND expires_at_utc > :now
                        AND is_revoked = :isRevoked
                    """;

            // When
            sut.getActiveTokensByStaffAccountId(staffAccountId);

            // Then
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            verify(jdbcHelperMock, times(1))
                    .query(sqlStatementCaptor.capture(), eq(RefreshTokenDao.class));

            SqlStatement actualStatement = sqlStatementCaptor.getValue();

            assertThat(actualStatement.sql()).isEqualTo(expectedSql);
            assertThat(actualStatement.params().build().get("staffAccountId")).isEqualTo(staffAccountId.getValue());
            assertThat(actualStatement.params().build().get("isRevoked")).isEqualTo(false);
            assertThat(actualStatement.params().build().get("now")).isNotNull();
        }

        @Test
        void shouldMapResponseFromJdbcHelperToExpectedResponse() {
            // Given
            List<RefreshTokenDao> refreshTokenDaos = List.of(
                    RefreshTokenDaoFixture.validRefreshTokenDao(),
                    RefreshTokenDaoFixture.validRefreshTokenDao()
            );

            when(jdbcHelperMock.query(any(SqlStatement.class), eq(RefreshTokenDao.class)))
                    .thenReturn(refreshTokenDaos);

            // When
            List<RefreshToken> refreshTokens = sut.getActiveTokensByStaffAccountId(StaffAccountId.generate());

            // Then
            assertThat(refreshTokens.size()).isEqualTo(refreshTokenDaos.size());
        }

        @Test
        void shouldPropagateInfraExceptionWhenJdbcHelperThrows() {
            // Given
            doThrow(InfraException.class)
                    .when(jdbcHelperMock)
                    .query(any(SqlStatement.class), eq(RefreshTokenDao.class));

            // When & Then
            assertThatThrownBy(() -> sut.getActiveTokensByStaffAccountId(StaffAccountId.generate()))
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class UpdateAll {
        private final WriteJdbcHelper jdbcHelperMock;
        private final RefreshTokenWriteRepoImpl sut;

        public UpdateAll() {
            jdbcHelperMock = mock(WriteJdbcHelper.class);
            sut = new RefreshTokenWriteRepoImpl(jdbcHelperMock);
        }

        @Test
        void shouldCallJdbcHelperWithCorrectSqlAndParams() {
            // Given
            List<RefreshToken> refreshTokens = List.of(RefreshTokenFixture.validRefreshToken(), RefreshTokenFixture.validRefreshToken());

            String expectedSql = """
                    UPDATE refresh_tokens
                    SET is_revoked = :isRevoked, revoked_at_utc = :revokedAtUtc, replaced_by = :replacedBy, version = :version, updated_at_utc = :updatedAtUtc
                    WHERE id = :id
                    """;

            ArgumentCaptor<List<SqlStatement>> sqlStatementsCaptor = ArgumentCaptor.forClass(List.class);

            // When
            sut.updateAll(refreshTokens);

            // Then
            verify(jdbcHelperMock, times(1)).executeMultiple(sqlStatementsCaptor.capture());

            List<SqlStatement> actualSqlStatements = sqlStatementsCaptor.getValue();
            assertThat(actualSqlStatements).hasSize(refreshTokens.size());

            for (RefreshToken token : refreshTokens) {
                assertThat(actualSqlStatements)
                        .anySatisfy(s -> {
                            assertThat(s.sql()).isEqualTo(expectedSql);
                            assertThat(s.params().build().get("id")).isEqualTo(token.getId().getValue());
                            assertThat(s.params().build().get("isRevoked")).isEqualTo(token.isRevoked());
                            assertThat(s.params().build().get("revokedAtUtc")).isEqualTo(token.getRevokedAt());
                            assertThat(s.params().build().get("version")).isEqualTo(token.getVersion().getValue());
                            assertThat(s.params().build().get("updatedAtUtc")).isNotNull();
                        });
            }
        }

        @Test
        void shouldPropagateInfraException() {
            // Given
            doThrow(InfraException.class)
                    .when(jdbcHelperMock)
                    .executeMultiple(anyList());

            List<RefreshToken> refreshTokens = List.of(RefreshTokenFixture.validRefreshToken());

            // When & Then
            assertThatExceptionOfType(InfraException.class)
                    .isThrownBy(() -> sut.updateAll(refreshTokens));
        }
    }
}
