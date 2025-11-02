package com.paragon.infrastructure.persistence.jdbc.helpers;

import com.paragon.infrastructure.persistence.jdbc.sql.SqlStatement;

import java.util.List;
import java.util.Optional;

public interface ReadJdbcHelper {
    <T> List<T> query(SqlStatement sqlStatement, Class<T> type);
    <T> Optional<T> queryFirstOrDefault(SqlStatement sqlStatement, Class<T> type);
}
