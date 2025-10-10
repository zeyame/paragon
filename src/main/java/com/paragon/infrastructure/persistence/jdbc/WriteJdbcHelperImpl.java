package com.paragon.infrastructure.persistence.jdbc;

import com.paragon.infrastructure.persistence.exceptions.InfraExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class WriteJdbcHelperImpl implements WriteJdbcHelper {
    @Qualifier("writeJdbcTemplate")
    private final NamedParameterJdbcTemplate jdbc;
    private final InfraExceptionHandler infraExceptionHandler;

    public WriteJdbcHelperImpl(NamedParameterJdbcTemplate jdbc, InfraExceptionHandler infraExceptionHandler) {
        this.jdbc = jdbc;
        this.infraExceptionHandler = infraExceptionHandler;
    }

    @Override
    public int execute(String sql, SqlParams params) {
        try {
            return jdbc.update(sql, params.build());
        } catch (DataAccessException e) {
            throw infraExceptionHandler.handleDatabaseException(e);
        }
    }

    @Override
    @Transactional
    public void executeMultiple(List<WriteQuery> queries) {
        try {
            for (WriteQuery query : queries) {
                jdbc.update(query.sql(), query.params().build());
            }
        } catch (DataAccessException e) {
            throw infraExceptionHandler.handleDatabaseException(e);
        }
    }

    @Override
    public <T> List<T> query(String sql, SqlParams params, Class<T> type) {
        try {
            return jdbc.query(sql, params.build(), BeanPropertyRowMapper.newInstance(type));
        } catch (DataAccessException e) {
            throw infraExceptionHandler.handleDatabaseException(e);
        }
    }

    @Override
    public <T> Optional<T> queryFirstOrDefault(String sql, SqlParams params, Class<T> type) {
        List<T> result = query(sql, params, type);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }
}
