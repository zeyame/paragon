package com.paragon.infrastructure.persistence.jdbc.helpers;

import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ReadJdbcHelperImpl implements ReadJdbcHelper {
    private final NamedParameterJdbcTemplate jdbc;

    public ReadJdbcHelperImpl(@Qualifier("readJdbcTemplate") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public <T> List<T> query(SqlStatement sqlStatement, Class<T> type) {
        return jdbc.query(sqlStatement.sql(), sqlStatement.params().build(), DataClassRowMapper.newInstance(type));
    }

    @Override
    public <T> Optional<T> queryFirstOrDefault(SqlStatement sqlStatement, Class<T> type) {
        List<T> result = query(sqlStatement, type);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }
}
