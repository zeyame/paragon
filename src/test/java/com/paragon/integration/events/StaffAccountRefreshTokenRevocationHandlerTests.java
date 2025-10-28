package com.paragon.integration.events;



import com.paragon.application.events.EventBusImpl;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountRefreshTokenRevocationHandlerTests extends IntegrationTestBase {
    private final EventBusImpl eventBus;
    private final TestJdbcHelper jdbcHelper;

    @Autowired
    public StaffAccountRefreshTokenRevocationHandlerTests(EventBusImpl eventBus, WriteJdbcHelper writeJdbcHelper) {
        this.eventBus = eventBus;
        this.jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
    }

    // TODO: Complete test when refresh token repo logic is done
    @Test
    void shouldRevokeStaffAccountRefreshTokens() {
        // Given
        StaffAccount lockedStaffAccount = new StaffAccountFixture()
                .withStatus(StaffAccountStatus.LOCKED)
                .withLockedUntil(Instant.now().plus(Duration.ofMinutes(15)))
                .withFailedLoginAttempts(5)
                .withCreatedBy(adminId)
                .build();
        jdbcHelper.insertStaffAccount(lockedStaffAccount);

        StaffAccountLockedEvent staffAccountLockedEvent = new StaffAccountLockedEvent(lockedStaffAccount);

        // When
        eventBus.publishAll(List.of(staffAccountLockedEvent));

        // Then
    }
}
