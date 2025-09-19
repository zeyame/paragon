package com.paragon.domain.models.valueobjects;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class VersionTests {

    @Test
    void givenInitialVersion_whenCreated_thenValueIsOne() {
        // Given
        Version version = Version.initial();

        // Then
        assertThat(version.getValue())
                .isEqualTo(1);
    }

    @Test
    void givenInitialVersion_whenIncreased_thenValueIsTwo() {
        // Given
        Version version = Version.initial();

        // When
        version = version.increase();

        // Then
        assertThat(version.getValue())
                .isEqualTo(2);
    }

    @Test
    void givenTwoVersionsWithTheSameValue_whenCompared_returnsTrue() {
        // Given
        Version version1 = Version.initial();
        Version version2 = Version.initial();

        // When & Then
        assertThat(version1).isEqualTo(version2);
    }
}
