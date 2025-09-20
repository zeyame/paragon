package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class VersionExceptionInfoTests {

    @Test
    void mustBeAtleastOne_shouldHaveExpectedCodeAndMessage() {
        // Given
        VersionExceptionInfo exceptionInfo = VersionExceptionInfo.mustBeAtleastOne();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("Version number cannot be below 1.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(100001);
    }
}
