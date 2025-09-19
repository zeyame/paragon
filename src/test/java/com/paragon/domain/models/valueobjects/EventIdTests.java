package com.paragon.domain.models.valueobjects;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class EventIdTests {
    @Nested
    class Generate {
        @Test
        void generate_returnsValidEventId() {
            EventId eventId = EventId.generate();
            assertThat(eventId.getValue().toString())
                    .isNotNull()
                    .hasSize(36);
        }
    }

}
