package com.paragon.infrastructure.persistence.jdbc.transaction;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class UnitOfWorkAwareDataSource implements DataSource {
    private final DataSource delegate;
    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public UnitOfWorkAwareDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = connectionHolder.get();

        if (connection != null) {
            // Return proxy that ignores close() - JdbcTemplate will call close() after each query,
            // but we need to keep the connection open until commit/rollback
            return createNonClosingProxy(connection);
        }

        // no active transaction - get new connection from pool
        return delegate.getConnection();
    }

    private Connection createNonClosingProxy(Connection target) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (InvocationHandler) (proxy, method, args) -> {
                    // Ignore close() calls during transaction
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    // Delegate everything else to the real connection
                    return method.invoke(target, args);
                }
        );
    }
    
    public Connection beginTransaction() throws SQLException {
        if (connectionHolder.get() != null) {
            throw new IllegalStateException("Transaction already active for this thread");
        }

        Connection connection = delegate.getConnection();
        connection.setAutoCommit(false);
        connectionHolder.set(connection);
        return connection;
    }

    
    public void commitTransaction() throws SQLException {
        Connection connection = connectionHolder.get();
        if (connection == null) {
            throw new IllegalStateException("No active transaction to commit");
        }

        try {
            connection.commit();
        } finally {
            cleanup(connection);
        }
    }

    
    public void rollbackTransaction() throws SQLException {
        Connection connection = connectionHolder.get();
        if (connection == null) {
            throw new IllegalStateException("No active transaction to rollback");
        }

        try {
            connection.rollback();
        } finally {
            cleanup(connection);
        }
    }
    
    public boolean isTransactionActive() {
        return connectionHolder.get() != null;
    }
    
    private void cleanup(Connection connection) throws SQLException {
        connectionHolder.remove();
        connection.setAutoCommit(true);
        connection.close();
    }

    // ========== DataSource Interface Methods (Delegate to underlying DataSource) ==========

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return delegate.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
}