package com.paragon.infrastructure.persistence.jdbc.transaction;

import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JdbcUnitOfWorkTests {

    @Nested
    class Begin {
        private final JdbcUnitOfWork sut;
        private final UnitOfWorkAwareDataSource dataSourceMock;

        public Begin() {
            dataSourceMock = mock(UnitOfWorkAwareDataSource.class);
            sut = new JdbcUnitOfWork(dataSourceMock);
        }

        @Test
        void shouldCallBeginTransactionOnDataSource() throws SQLException {
            // When
            sut.begin();

            // Then
            verify(dataSourceMock, times(1)).beginTransaction();
        }

        @Test
        void shouldThrowInfraException_whenSqlExceptionOccurs() throws SQLException {
            // Given
            doThrow(new SQLException("Database error"))
                    .when(dataSourceMock).beginTransaction();

            // When & Then
            assertThatThrownBy(sut::begin)
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class Commit {
        private final JdbcUnitOfWork sut;
        private final UnitOfWorkAwareDataSource dataSourceMock;

        public Commit() {
            dataSourceMock = mock(UnitOfWorkAwareDataSource.class);
            sut = new JdbcUnitOfWork(dataSourceMock);
        }

        @Test
        void shouldCallCommitTransactionOnDataSource() throws SQLException {
            // When
            sut.commit();

            // Then
            verify(dataSourceMock, times(1)).commitTransaction();
        }

        @Test
        void shouldThrowInfraException_whenSqlExceptionOccurs() throws SQLException {
            // Given
            doThrow(new SQLException("Commit failed")).when(dataSourceMock).commitTransaction();

            // When & Then
            assertThatThrownBy(sut::commit)
                    .isInstanceOf(InfraException.class);
        }
    }

    @Nested
    class Rollback {
        private final JdbcUnitOfWork sut;
        private final UnitOfWorkAwareDataSource dataSourceMock;

        public Rollback() {
            dataSourceMock = mock(UnitOfWorkAwareDataSource.class);
            sut = new JdbcUnitOfWork(dataSourceMock);
        }

        @Test
        void shouldCallRollbackTransactionOnDataSource() throws SQLException {
            // When
            sut.rollback();

            // Then
            verify(dataSourceMock, times(1)).rollbackTransaction();
        }

        @Test
        void shouldThrowInfraException_whenSqlExceptionOccurs() throws SQLException {
            // Given
            doThrow(new SQLException("Rollback failed")).when(dataSourceMock).rollbackTransaction();

            // When & Then
            assertThatThrownBy(sut::rollback)
                    .isInstanceOf(InfraException.class);
        }
    }
}