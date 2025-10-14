package com.paragon.infrastructure.persistence.jdbc;

public record WriteQuery(String sql, SqlParamsBuilder params)
{}
