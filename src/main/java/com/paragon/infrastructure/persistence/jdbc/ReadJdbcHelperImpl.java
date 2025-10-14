package com.paragon.infrastructure.persistence.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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
    public <T> List<T> query(String sql, SqlParamsBuilder params, Class<T> type) {
        return jdbc.query(sql, params.build(), BeanPropertyRowMapper.newInstance(type));
    }

    @Override
    public <T> Optional<T> queryFirstOrDefault(String sql, SqlParamsBuilder params, Class<T> type) {
        List<T> result = query(sql, params, type);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }
}
