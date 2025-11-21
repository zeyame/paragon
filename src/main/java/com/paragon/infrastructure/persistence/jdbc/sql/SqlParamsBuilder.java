package com.paragon.infrastructure.persistence.jdbc.sql;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SqlParamsBuilder {
    private final Map<String, Object> values = new HashMap<>();

    public SqlParamsBuilder add(String key, Object value) {
        if (value instanceof Instant instant) {
            values.put(key, Timestamp.from(instant));
        } else {
            values.put(key, value);
        }
        return this;
    }

    public Object get(String key) {
        return values.get(key);
    }

    public Map<String, Object> build() {
        return Collections.unmodifiableMap(new HashMap<>(values));
    }
}
