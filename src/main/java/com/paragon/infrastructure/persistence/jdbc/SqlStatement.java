package com.paragon.infrastructure.persistence.jdbc;

public record SqlStatement(String sql, SqlParamsBuilder params)
{}
