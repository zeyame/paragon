package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.interfaces.repositories.StaffAccountPasswordHistoryWriteRepo;
import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.fixtures.PasswordHistoryEntryFixture;
import com.paragon.infrastructure.persistence.daos.PasswordHistoryEntryDao;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import com.paragon.infrastructure.persistence.repos.write.StaffAccountPasswordHistoryWriteRepoImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class StaffAccountPasswordHistoryWriteRepoTests {
    @Nested
    class AppendEntry {
        private final StaffAccountPasswordHistoryWriteRepo sut;
        private final WriteJdbcHelper writeJdbcHelperMock;

        public AppendEntry() {
            writeJdbcHelperMock = mock(WriteJdbcHelper.class);
            sut = new StaffAccountPasswordHistoryWriteRepoImpl(writeJdbcHelperMock);
        }

        @Test
        void shouldAppendEntry_withCorrectSqlAndParams() {
            // Given
            String expectedSql = """
                        INSERT INTO staff_account_password_history
                        (id, staff_account_id, hashed_password, is_temporary, changed_at_utc)
                        VALUES
                        (:id, :staffAccountId, :hashedPassword, :isTemporary, :changedAtUtc)
                    """;

            PasswordHistoryEntry entry = PasswordHistoryEntryFixture.validEntry();

            // When
            sut.appendEntry(entry);

            // Then
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);
            verify(writeJdbcHelperMock, times(1)).execute(sqlStatementCaptor.capture());

            SqlStatement sqlStatement = sqlStatementCaptor.getValue();
            assertThat(sqlStatement.sql()).isEqualTo(expectedSql);
            assertThat(sqlStatement.params().get("id")).isInstanceOf(UUID.class);
            assertThat(sqlStatement.params().get("staffAccountId")).isEqualTo(entry.staffAccountId().getValue());
            assertThat(sqlStatement.params().get("hashedPassword")).isEqualTo(entry.hashedPassword().getValue());
            assertThat(sqlStatement.params().get("isTemporary")).isEqualTo(entry.isTemporary());
            assertThat(sqlStatement.params().get("changedAtUtc")).isEqualTo(Timestamp.from(entry.changedAt().getValue()));
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            doThrow(InfraException.class)
                    .when(writeJdbcHelperMock)
                    .execute(any(SqlStatement.class));

            // When & Then
            assertThatThrownBy(() -> sut.appendEntry(PasswordHistoryEntryFixture.validEntry()))
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class GetPasswordHistory {
        private final StaffAccountPasswordHistoryWriteRepo sut;
        private final WriteJdbcHelper writeJdbcHelperMock;

        public GetPasswordHistory() {
            writeJdbcHelperMock = mock(WriteJdbcHelper.class);
            sut = new StaffAccountPasswordHistoryWriteRepoImpl(writeJdbcHelperMock);
        }

        @Test
        void shouldExecuteQueryWithCorrectSqlAndParams() {
            // Given
            StaffAccountId staffAccountId = StaffAccountId.generate();
            String expectedSql = """
                        SELECT * FROM staff_account_password_history
                        WHERE staff_account_id = :staffAccountId
                    """;

            when(writeJdbcHelperMock.query(any(SqlStatement.class), eq(PasswordHistoryEntryDao.class)))
                    .thenReturn(List.of(createPasswordHistoryEntryDao(staffAccountId)));

            // When
            sut.getPasswordHistory(staffAccountId);

            // Then
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);
            verify(writeJdbcHelperMock, times(1))
                    .query(sqlStatementCaptor.capture(), eq(PasswordHistoryEntryDao.class));

            SqlStatement sqlStatement = sqlStatementCaptor.getValue();
            assertThat(sqlStatement.sql()).isEqualTo(expectedSql);
            assertThat(sqlStatement.params().get("staffAccountId")).isEqualTo(staffAccountId.getValue());
        }

        private PasswordHistoryEntryDao createPasswordHistoryEntryDao(StaffAccountId staffAccountId) {
            return new PasswordHistoryEntryDao(
                    UUID.randomUUID(), staffAccountId.getValue(),
                    "hashed-password", false, Instant.now()
            );
        }
    }
}
