package com.paragon.infrastructure.persistence.repos;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.helpers.fixtures.AuditTrailEntryFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.write.AuditTrailWriteRepoImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuditTrailWriteRepoTests {
    @Nested
    class Create {
        private final WriteJdbcHelper jdbcHelperMock;
        private final AuditTrailWriteRepoImpl sut;

        public Create() {
            this.jdbcHelperMock = mock(WriteJdbcHelper.class);
            this.sut = new AuditTrailWriteRepoImpl(jdbcHelperMock);
        }

        @Test
        void callsJdbcHelper_withExpectedSqlAndParams() {
            // Given
            var auditTrailEntry = AuditTrailEntryFixture.validAuditTrailEntry();
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(sqlStatementCaptor.capture());

            SqlStatement statement = sqlStatementCaptor.getValue();

            assertThat(statement.sql()).contains("INSERT INTO audit_trail");
            assertThat(statement.params().build().get("id")).isEqualTo(auditTrailEntry.getId().getValue());
            assertThat(statement.params().build().get("actorId")).isEqualTo(auditTrailEntry.getActorId().getValue());
            assertThat(statement.params().build().get("actionType")).isEqualTo(auditTrailEntry.getActionType().toString());
            assertThat(statement.params().build().get("targetId")).isEqualTo(auditTrailEntry.getTargetId().getValue());
            assertThat(statement.params().build().get("targetType")).isEqualTo(auditTrailEntry.getTargetType().toString());
        }

        @Test
        void handlesNullTargetId_correctly() {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withTargetId(null)
                    .build();
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(sqlStatementCaptor.capture());
            SqlStatement statement = sqlStatementCaptor.getValue();

            assertThat(statement.params().build().get("targetId")).isNull();
        }

        @Test
        void handlesNullTargetType_correctly() {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withTargetType(null)
                    .build();
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(sqlStatementCaptor.capture());
            SqlStatement statement = sqlStatementCaptor.getValue();

            assertThat(statement.params().build().get("targetType")).isNull();
        }

        @Test
        void handlesActionType_correctly() {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withActionType(AuditEntryActionType.LOGIN)
                    .build();
            ArgumentCaptor<SqlStatement> sqlStatementCaptor = ArgumentCaptor.forClass(SqlStatement.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(sqlStatementCaptor.capture());
            SqlStatement statement = sqlStatementCaptor.getValue();

            assertThat(statement.params().build().get("actionType")).isEqualTo("LOGIN");
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            doThrow(InfraException.class)
                    .when(jdbcHelperMock)
                    .execute(any(SqlStatement.class));

            // When & Then
            assertThatThrownBy(() -> sut.create(AuditTrailEntryFixture.validAuditTrailEntry()))
                    .isInstanceOf(InfraException.class);
        }
    }
}
