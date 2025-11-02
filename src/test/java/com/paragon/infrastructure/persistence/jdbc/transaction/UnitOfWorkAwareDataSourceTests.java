package com.paragon.infrastructure.persistence.jdbc.transaction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UnitOfWorkAwareDataSourceTests {

    @Nested
    class BeginTransaction {
        private final UnitOfWorkAwareDataSource sut;
        private final DataSource dataSourceMock;
        private final Connection connectionMock;

        public BeginTransaction() throws SQLException {
            dataSourceMock = mock(DataSource.class);
            connectionMock = mock(Connection.class);
            when(dataSourceMock.getConnection()).thenReturn(connectionMock);

            sut = new UnitOfWorkAwareDataSource(dataSourceMock);
        }

        @Test
        void shouldGetConnectionFromDataSource() throws SQLException {
            // When
            sut.beginTransaction();

            // Then
            verify(dataSourceMock, times(1)).getConnection();
        }

        @Test
        void shouldDisableAutoCommitOnConnection() throws SQLException {
            // When
            sut.beginTransaction();

            // Then
            verify(connectionMock, times(1)).setAutoCommit(false);
        }

        @Test
        void shouldReturnTheConnectionThatWasAcquired() throws SQLException {
            // When
            Connection connection = sut.beginTransaction();

            // Then
            assertThat(connection).isEqualTo(connectionMock);
        }

        @Test
        void shouldThrowException_whenTransactionAlreadyActive() throws SQLException {
            // Given
            sut.beginTransaction();

            // When & Then
            assertThatThrownBy(() -> sut.beginTransaction())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transaction already active");
        }
    }

    @Nested
    class GetConnection {
        private final UnitOfWorkAwareDataSource sut;
        private final DataSource dataSourceMock;
        private final Connection transactionConnectionMock;
        private final Connection newConnectionMock;

        public GetConnection() throws SQLException {
            dataSourceMock = mock(DataSource.class);
            transactionConnectionMock = mock(Connection.class, "transactionConnection");
            newConnectionMock = mock(Connection.class, "newConnection");

            when(dataSourceMock.getConnection())
                    .thenReturn(transactionConnectionMock)
                    .thenReturn(newConnectionMock);

            sut = new UnitOfWorkAwareDataSource(dataSourceMock);
        }

        @Test
        void shouldReturnTransactionConnection_whenTransactionIsActive() throws SQLException {
            // Given
            sut.beginTransaction(); // Gets transactionConnectionMock

            // When
            Connection connection = sut.getConnection();

            // Then
            assertThat(connection).isEqualTo(transactionConnectionMock);
            verify(dataSourceMock, times(1)).getConnection(); // Only called once during begin
        }

        @Test
        void shouldReturnNewConnection_whenNoTransactionIsActive() throws SQLException {
            // When
            Connection connection = sut.getConnection();

            // Then
            assertThat(connection).isNotNull();
            verify(dataSourceMock, times(1)).getConnection();
        }
    }

    @Nested
    class CommitTransaction {
        private final UnitOfWorkAwareDataSource sut;
        private final DataSource dataSourceMock;
        private final Connection connectionMock;

        public CommitTransaction() throws SQLException {
            dataSourceMock = mock(DataSource.class);
            connectionMock = mock(Connection.class);
            when(dataSourceMock.getConnection()).thenReturn(connectionMock);

            sut = new UnitOfWorkAwareDataSource(dataSourceMock);
        }

        @Test
        void shouldCommitTheConnection() throws SQLException {
            // Given
            sut.beginTransaction();

            // When
            sut.commitTransaction();

            // Then
            verify(connectionMock, times(1)).commit();
        }

        @Test
        void shouldResetAutoCommitToTrue() throws SQLException {
            // Given
            sut.beginTransaction();

            // When
            sut.commitTransaction();

            // Then
            verify(connectionMock, times(1)).setAutoCommit(true);
        }

        @Test
        void shouldCloseConnection() throws SQLException {
            // Given
            sut.beginTransaction();

            // When
            sut.commitTransaction();

            // Then
            verify(connectionMock, times(1)).close();
        }

        @Test
        void shouldClearThreadLocalConnection() throws SQLException {
            // Given
            sut.beginTransaction();

            // When
            sut.commitTransaction();

            // Then - next getConnection should get new connection, not cached one
            assertThat(sut.isTransactionActive()).isFalse();
        }

        @Test
        void shouldThrowException_whenNoActiveTransaction() {
            // When & Then
            assertThatThrownBy(() -> sut.commitTransaction())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No active transaction");
        }

        @Test
        void shouldCloseConnection_evenIfCommitFails() throws SQLException {
            // Given
            sut.beginTransaction();
            doThrow(new SQLException("Commit failed")).when(connectionMock).commit();

            // When & Then
            assertThatThrownBy(() -> sut.commitTransaction())
                    .isInstanceOf(SQLException.class);

            // Verify cleanup still happened
            verify(connectionMock, times(1)).setAutoCommit(true);
            verify(connectionMock, times(1)).close();
        }
    }

    @Nested
    class RollbackTransaction {
        private final UnitOfWorkAwareDataSource sut;
        private final DataSource dataSourceMock;
        private final Connection connectionMock;

        public RollbackTransaction() throws SQLException {
            dataSourceMock = mock(DataSource.class);
            connectionMock = mock(Connection.class);
            when(dataSourceMock.getConnection()).thenReturn(connectionMock);

            sut = new UnitOfWorkAwareDataSource(dataSourceMock);
        }

        @Test
        void shouldRollbackTheConnection() throws SQLException {
            // Given
            sut.beginTransaction();

            // When
            sut.rollbackTransaction();

            // Then
            verify(connectionMock, times(1)).rollback();
        }

        @Test
        void shouldResetAutoCommitToTrue() throws SQLException {
            // Given
            sut.beginTransaction();

            // When
            sut.rollbackTransaction();

            // Then
            verify(connectionMock, times(1)).setAutoCommit(true);
        }

        @Test
        void shouldCloseConnection() throws SQLException {
            // Given
            sut.beginTransaction();

            // When
            sut.rollbackTransaction();

            // Then
            verify(connectionMock, times(1)).close();
        }

        @Test
        void shouldClearThreadLocalConnection() throws SQLException {
            // Given
            sut.beginTransaction();

            // When
            sut.rollbackTransaction();

            // Then
            assertThat(sut.isTransactionActive()).isFalse();
        }

        @Test
        void shouldThrowException_whenNoActiveTransaction() {
            // When & Then
            assertThatThrownBy(() -> sut.rollbackTransaction())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No active transaction");
        }

        @Test
        void shouldCloseConnection_evenIfRollbackFails() throws SQLException {
            // Given
            sut.beginTransaction();
            doThrow(new SQLException("Rollback failed")).when(connectionMock).rollback();

            // When & Then
            assertThatThrownBy(() -> sut.rollbackTransaction())
                    .isInstanceOf(SQLException.class);

            // Verify cleanup still happened
            verify(connectionMock, times(1)).setAutoCommit(true);
            verify(connectionMock, times(1)).close();
        }
    }
}