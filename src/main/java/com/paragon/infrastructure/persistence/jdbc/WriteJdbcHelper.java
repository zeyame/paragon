package com.paragon.infrastructure.persistence.jdbc;

import java.util.List;
import java.util.Optional;

public interface WriteJdbcHelper {
    int execute(String sql, SqlParams params);
    <T> List<T> query(String sql, SqlParams params, Class<T> type);
    <T> Optional<T> queryFirstOrDefault(String sql, SqlParams params, Class<T> type);
}
