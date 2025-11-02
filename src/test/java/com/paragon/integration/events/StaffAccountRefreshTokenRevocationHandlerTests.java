package com.paragon.integration.events;

import com.paragon.application.events.EventBusImpl;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffAccountRefreshTokenRevocationHandlerTests extends IntegrationTestBase {
    private final EventBusImpl eventBus;
    private final TestJdbcHelper jdbcHelper;

    @Autowired
    public StaffAccountRefreshTokenRevocationHandlerTests(EventBusImpl eventBus, WriteJdbcHelper writeJdbcHelper) {
        this.eventBus = eventBus;
        this.jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
    }

    @Test
    void shouldRevokeStaffAccountRefreshTokens_uponStaffAccountLockedEvent() {
        // Given
        StaffAccount lockedStaffAccount = new StaffAccountFixture()
                .withStatus(StaffAccountStatus.LOCKED)
                .withLockedUntil(Instant.now().plus(Duration.ofMinutes(15)))
                .withFailedLoginAttempts(5)
                .withCreatedBy(adminId)
                .build();
        jdbcHelper.insertStaffAccount(lockedStaffAccount);

        RefreshToken refreshToken1 = new RefreshTokenFixture()
                .withStaffAccountId(lockedStaffAccount.getId().getValue().toString())
                .build();
        jdbcHelper.insertRefreshToken(refreshToken1);

        RefreshToken refreshToken2 = new RefreshTokenFixture()
                .withStaffAccountId(lockedStaffAccount.getId().getValue().toString())
                .build();
        jdbcHelper.insertRefreshToken(refreshToken2);

        StaffAccountLockedEvent staffAccountLockedEvent = new StaffAccountLockedEvent(lockedStaffAccount);

        // When
        eventBus.publishAll(List.of(staffAccountLockedEvent));

        // Then
        List<RefreshToken> retrievedTokens = jdbcHelper.getAllRefreshTokensByStaffAccountId(lockedStaffAccount.getId());
        assertThat(retrievedTokens).allSatisfy(token -> {
            assertThat(token.isRevoked()).isTrue();
            assertThat(token.getRevokedAt()).isNotNull();
            assertThat(token.getVersion().getValue()).isGreaterThan(1);
        });
    }
}
