package com.paragon.infrastructure.persistence;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.helpers.fixtures.AuditTrailEntryFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.SqlParamsBuilder;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.AuditTrailWriteRepoImpl;
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
            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<SqlParamsBuilder> paramsCaptor = ArgumentCaptor.forClass(SqlParamsBuilder.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(sqlCaptor.capture(), paramsCaptor.capture());

            String sql = sqlCaptor.getValue();
            SqlParamsBuilder params = paramsCaptor.getValue();

            assertThat(sql).contains("INSERT INTO audit_trail");
            assertThat(params.build().get("id")).isEqualTo(auditTrailEntry.getId().getValue());
            assertThat(params.build().get("actorId")).isEqualTo(auditTrailEntry.getActorId().getValue());
            assertThat(params.build().get("actionType")).isEqualTo(auditTrailEntry.getActionType().toString());
            assertThat(params.build().get("targetId")).isEqualTo(auditTrailEntry.getTargetId().getValue());
            assertThat(params.build().get("targetType")).isEqualTo(auditTrailEntry.getTargetType().toString());
        }

        @Test
        void handlesNullTargetId_correctly() {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withTargetId(null)
                    .build();
            ArgumentCaptor<SqlParamsBuilder> paramsCaptor = ArgumentCaptor.forClass(SqlParamsBuilder.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(anyString(), paramsCaptor.capture());
            SqlParamsBuilder params = paramsCaptor.getValue();

            assertThat(params.build().get("targetId")).isNull();
        }

        @Test
        void handlesNullTargetType_correctly() {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withTargetType(null)
                    .build();
            ArgumentCaptor<SqlParamsBuilder> paramsCaptor = ArgumentCaptor.forClass(SqlParamsBuilder.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(anyString(), paramsCaptor.capture());
            SqlParamsBuilder params = paramsCaptor.getValue();

            assertThat(params.build().get("targetType")).isNull();
        }

        @Test
        void handlesActionType_correctly() {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withActionType(AuditEntryActionType.LOGIN)
                    .build();
            ArgumentCaptor<SqlParamsBuilder> paramsCaptor = ArgumentCaptor.forClass(SqlParamsBuilder.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(anyString(), paramsCaptor.capture());
            SqlParamsBuilder params = paramsCaptor.getValue();

            assertThat(params.build().get("actionType")).isEqualTo("LOGIN");
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            doThrow(InfraException.class)
                    .when(jdbcHelperMock)
                    .execute(anyString(), any(SqlParamsBuilder.class));

            // When & Then
            assertThatThrownBy(() -> sut.create(AuditTrailEntryFixture.validAuditTrailEntry()))
                    .isInstanceOf(InfraException.class);
        }
    }
}
