package com.paragon.infrastructure.persistence.jdbc.transaction;

import com.paragon.application.common.interfaces.UnitOfWork;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class JdbcUnitOfWork implements UnitOfWork {
    private final UnitOfWorkAwareDataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(JdbcUnitOfWork.class);

    public JdbcUnitOfWork(UnitOfWorkAwareDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void begin() {
        try {
            dataSource.beginTransaction();
            log.debug("Transaction started for thread: {}", Thread.currentThread().getName());
        } catch (SQLException e) {
            log.error("Failed to begin transaction", e);
            throw new InfraException();
        }
    }

    @Override
    public void commit() {
        try {
            dataSource.commitTransaction();
            log.debug("Transaction committed for thread: {}", Thread.currentThread().getName());
        } catch (SQLException e) {
            log.error("Failed to commit transaction", e);
            throw new InfraException();
        }
    }

    @Override
    public void rollback() {
        try {
            dataSource.rollbackTransaction();
            log.debug("Transaction rolled back for thread: {}", Thread.currentThread().getName());
        } catch (SQLException e) {
            log.error("Failed to rollback transaction", e);
            throw new InfraException();
        }
    }
}
