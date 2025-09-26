package com.paragon.infrastructure.persistence.jdbc;

import java.util.HashMap;
import java.util.Map;

public class SqlParams {
    private final Map<String, Object> values = new HashMap<>();

    public SqlParams add(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return Map.copyOf(values);
    }
}
