package com.paragon.infrastructure.persistence.jdbc;

import com.paragon.infrastructure.persistence.exceptions.InfraExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class WriteJdbcHelperImpl implements WriteJdbcHelper {
    private final NamedParameterJdbcTemplate jdbc;
    private final InfraExceptionHandler infraExceptionHandler;

    public WriteJdbcHelperImpl(@Qualifier("writeJdbcTemplate") NamedParameterJdbcTemplate jdbc, InfraExceptionHandler infraExceptionHandler) {
        this.jdbc = jdbc;
        this.infraExceptionHandler = infraExceptionHandler;
    }

    @Override
    public int execute(SqlStatement sqlStatement) {
        try {
            return jdbc.update(sqlStatement.sql(), sqlStatement.params().build());
        } catch (DataAccessException e) {
            throw infraExceptionHandler.handleDatabaseException(e);
        }
    }

    @Override
    @Transactional
    public void executeMultiple(List<SqlStatement> queries) {
        try {
            for (SqlStatement query : queries) {
                jdbc.update(query.sql(), query.params().build());
            }
        } catch (DataAccessException e) {
            log.error("e: ", e);
            throw infraExceptionHandler.handleDatabaseException(e);
        }
    }

    @Override
    public <T> List<T> query(SqlStatement sqlStatement, Class<T> type) {
        try {
            return jdbc.query(sqlStatement.sql(), sqlStatement.params().build(), DataClassRowMapper.newInstance(type));
        } catch (DataAccessException e) {
            log.error("e: ", e);
            throw infraExceptionHandler.handleDatabaseException(e);
        }
    }

    @Override
    public <T> Optional<T> queryFirstOrDefault(SqlStatement sqlStatement, Class<T> type) {
        List<T> result = query(sqlStatement, type);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }
}
