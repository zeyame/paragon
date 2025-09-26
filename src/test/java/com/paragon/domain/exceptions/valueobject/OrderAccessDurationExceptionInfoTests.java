package com.paragon.domain.exceptions.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderAccessDurationExceptionInfoTests {

    @Test
    void mustBePositive_shouldHaveExpectedCodeAndMessage() {
        OrderAccessDurationExceptionInfo info = OrderAccessDurationExceptionInfo.mustBePositive();

        assertThat(info.getMessage()).isEqualTo("Order access duration must be greater than zero");
        assertThat(info.getDomainErrorCode()).isEqualTo(106001);
    }
}
