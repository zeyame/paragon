package com.paragon.infrastructure.persistence.jdbc.sql;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SqlParamsBuilderTests {
    @Nested
    class Add {
        @Test
        void add_shouldStoreValues() {
            // When
            SqlParamsBuilder builder = new SqlParamsBuilder()
                    .add("id", "123")
                    .add("count", 5);

            Map<String, Object> params = builder.build();

            // Then
            assertThat(params).containsEntry("id", "123");
            assertThat(params).containsEntry("count", 5);
        }

        @Test
        void add_shouldConvertInstantToTimestamp() {
            // Given
            Instant now = Instant.now();

            // When
            SqlParamsBuilder builder = new SqlParamsBuilder().add("createdAt", now);

            // Then
            Object storedValue = builder.build().get("createdAt");
            assertThat(storedValue).isInstanceOf(Timestamp.class);
            assertThat(((Timestamp) storedValue).toInstant()).isEqualTo(now);
        }
    }

    @Nested
    class Get {
        @Test
        void shouldReturnCorrectValue() {
            // Given
            SqlParamsBuilder builder = new SqlParamsBuilder().add("username", "john_doe");

            // When & Then
            assertThat(builder.get("username")).isEqualTo("john_doe");
        }

        @Test
        void shouldReturnNullWhenValueDoesNotExist() {
            // Given
            SqlParamsBuilder builder = new SqlParamsBuilder().add("username", "john_doe");

            // When & Then
            assertThat(builder.get("missing")).isNull();
        }
    }
}
