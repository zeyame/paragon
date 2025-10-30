package com.paragon.infrastructure.persistence.jdbc;

import java.util.List;
import java.util.Optional;

public interface WriteJdbcHelper {
    int execute(SqlStatement sqlStatement);
    void executeMultiple(List<SqlStatement> sqlStatements);
    <T> List<T> query(SqlStatement sqlStatement, Class<T> type);
    <T> Optional<T> queryFirstOrDefault(SqlStatement sqlStatement, Class<T> type);
}
