package com.paragon.infrastructure.persistence.jdbc;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Optional;

public class WriteJdbcHelperImpl implements WriteJdbcHelper {
    private final NamedParameterJdbcTemplate jdbc;

    public WriteJdbcHelperImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int execute(String sql, SqlParams params) {
        return jdbc.update(sql, params.build());
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
