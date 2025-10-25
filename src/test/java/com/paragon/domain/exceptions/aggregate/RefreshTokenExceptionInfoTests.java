package com.paragon.domain.exceptions.aggregate;

import com.paragon.domain.exceptions.valueobject.RefreshTokenHashExceptionInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenExceptionInfoTests {
    @Test
    void staffAccountIdRequired_shouldHaveExpectedCodeAndMessage() {
        // When
        RefreshTokenExceptionInfo exceptionInfo = RefreshTokenExceptionInfo.staffAccountIdRequired();

        // Then
        assertThat(exceptionInfo.getMessage()).isEqualTo("A staff account id is required to generate a refresh token.");
        assertThat(exceptionInfo.getDomainErrorCode()).isEqualTo(20001);
    }
}
