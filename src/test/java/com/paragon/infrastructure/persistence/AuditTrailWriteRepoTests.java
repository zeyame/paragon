package com.paragon.infrastructure.persistence;

import com.paragon.domain.enums.AuditEntryActionType;
import com.paragon.domain.enums.Outcome;
import com.paragon.helpers.fixtures.AuditTrailEntryFixture;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import com.paragon.infrastructure.persistence.jdbc.SqlParams;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.AuditTrailWriteRepoImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.stream.Stream;

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
            ArgumentCaptor<SqlParams> paramsCaptor = ArgumentCaptor.forClass(SqlParams.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(sqlCaptor.capture(), paramsCaptor.capture());

            String sql = sqlCaptor.getValue();
            SqlParams params = paramsCaptor.getValue();

            assertThat(sql).contains("INSERT INTO audit_trail");
            assertThat(params.build().get("id")).isEqualTo(auditTrailEntry.getId().getValue());
            assertThat(params.build().get("actorId")).isEqualTo(auditTrailEntry.getActorId().getValue());
            assertThat(params.build().get("actionType")).isEqualTo(auditTrailEntry.getActionType().toString());
            assertThat(params.build().get("targetId")).isEqualTo(auditTrailEntry.getTargetId().getValue());
            assertThat(params.build().get("targetType")).isEqualTo(auditTrailEntry.getTargetType().toString());
            assertThat(params.build().get("outcome")).isEqualTo(auditTrailEntry.getOutcome().toString());
            assertThat(params.build().get("ipAddress")).isEqualTo(auditTrailEntry.getIpAddress());
            assertThat(params.build().get("correlationId")).isEqualTo(auditTrailEntry.getCorrelationId());
        }

        @Test
        void handlesNullTargetId_correctly() {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withTargetId(null)
                    .build();
            ArgumentCaptor<SqlParams> paramsCaptor = ArgumentCaptor.forClass(SqlParams.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(anyString(), paramsCaptor.capture());
            SqlParams params = paramsCaptor.getValue();

            assertThat(params.build().get("targetId")).isNull();
        }

        @Test
        void handlesNullTargetType_correctly() {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withTargetType(null)
                    .build();
            ArgumentCaptor<SqlParams> paramsCaptor = ArgumentCaptor.forClass(SqlParams.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(anyString(), paramsCaptor.capture());
            SqlParams params = paramsCaptor.getValue();

            assertThat(params.build().get("targetType")).isNull();
        }

        @Test
        void handlesActionType_correctly() {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withActionType(AuditEntryActionType.LOGIN)
                    .build();
            ArgumentCaptor<SqlParams> paramsCaptor = ArgumentCaptor.forClass(SqlParams.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(anyString(), paramsCaptor.capture());
            SqlParams params = paramsCaptor.getValue();

            assertThat(params.build().get("actionType")).isEqualTo("LOGIN");
        }

        @ParameterizedTest
        @MethodSource("auditTrailOutcomes")
        void handlesAllOutcomes_correctly(Outcome outcome) {
            // Given
            var auditTrailEntry = new AuditTrailEntryFixture()
                    .withOutcome(outcome)
                    .build();
            ArgumentCaptor<SqlParams> paramsCaptor = ArgumentCaptor.forClass(SqlParams.class);

            // When
            sut.create(auditTrailEntry);

            // Then
            verify(jdbcHelperMock, times(1)).execute(anyString(), paramsCaptor.capture());
            SqlParams params = paramsCaptor.getValue();

            assertThat(params.build().get("outcome")).isEqualTo(outcome.toString());
        }

        @Test
        void shouldPropagateInfraException_whenJdbcHelperThrows() {
            // Given
            doThrow(InfraException.class)
                    .when(jdbcHelperMock)
                    .execute(anyString(), any(SqlParams.class));

            // When & Then
            assertThatThrownBy(() -> sut.create(AuditTrailEntryFixture.validAuditTrailEntry()))
                    .isInstanceOf(InfraException.class);
        }

        private static Stream<Arguments> auditTrailOutcomes() {
            return Stream.of(
                    Arguments.of(Outcome.SUCCESS),
                    Arguments.of(Outcome.FAILURE)
            );
        }
    }
}
