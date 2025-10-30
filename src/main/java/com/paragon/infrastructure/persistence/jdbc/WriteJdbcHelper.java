package com.paragon.infrastructure.persistence.jdbc;

import java.util.List;
import java.util.Optional;

public interface WriteJdbcHelper {
    int execute(String sql, SqlParamsBuilder params);
    void executeMultiple(List<SqlStatement> sqlStatements);
    <T> List<T> query(String sql, SqlParamsBuilder params, Class<T> type);
    <T> Optional<T> queryFirstOrDefault(String sql, SqlParamsBuilder params, Class<T> type);
}
