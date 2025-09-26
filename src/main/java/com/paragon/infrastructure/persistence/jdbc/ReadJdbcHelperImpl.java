package com.paragon.infrastructure.persistence.jdbc;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Optional;

public class ReadJdbcHelperImpl implements ReadJdbcHelper {
    private final NamedParameterJdbcTemplate jdbc;

    public ReadJdbcHelperImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public <T> List<T> query(String sql, SqlParams params, Class<T> type) {
        return jdbc.query(sql, params.build(), BeanPropertyRowMapper.newInstance(type));
    }

    @Override
    public <T> Optional<T> queryFirstOrDefault(String sql, SqlParams params, Class<T> type) {
        List<T> result = query(sql, params, type);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }
}
