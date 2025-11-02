package com.paragon.infrastructure.persistence.jdbc.sql;

public record SqlStatement(String sql, SqlParamsBuilder params)
{}
