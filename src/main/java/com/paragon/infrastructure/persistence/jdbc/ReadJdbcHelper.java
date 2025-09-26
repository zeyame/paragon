package com.paragon.infrastructure.persistence.jdbc;

import com.paragon.domain.models.entities.Permission;

import java.util.List;
import java.util.Optional;

public interface ReadJdbcHelper {
    <T> List<T> query(String sql, SqlParams params, Class<T> type);
    <T> Optional<T> queryFirstOrDefault(String sql, SqlParams params, Class<T> type);
}
