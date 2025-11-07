package com.paragon.integration.events;

import com.paragon.application.events.EventBusImpl;
import com.paragon.domain.events.staffaccountevents.StaffAccountDisabledEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountLockedEvent;
import com.paragon.domain.events.staffaccountevents.StaffAccountPasswordResetEvent;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    void shouldRevokeRefreshTokens_whenStaffAccountLocked() {
        // Given
        StaffAccount staffAccount = createAndInsertStaffAccount();
        insertRefreshTokensFor(staffAccount);

        // When
        eventBus.publishAll(List.of(new StaffAccountLockedEvent(staffAccount)));

        // Then
        assertAllTokensRevoked(staffAccount);
    }

    @Test
    void shouldRevokeRefreshTokens_whenStaffAccountDisabled() {
        // Given
        StaffAccount staffAccount = createAndInsertStaffAccount();
        insertRefreshTokensFor(staffAccount);

        // When
        eventBus.publishAll(List.of(new StaffAccountDisabledEvent(staffAccount)));

        // Then
        assertAllTokensRevoked(staffAccount);
    }

    @Test
    void shouldRevokeRefreshTokens_whenStaffAccountPasswordReset() {
        // Given
        StaffAccount staffAccount = createAndInsertStaffAccount();
        insertRefreshTokensFor(staffAccount);

        // When
        eventBus.publishAll(List.of(new StaffAccountPasswordResetEvent(staffAccount)));

        // Then
        assertAllTokensRevoked(staffAccount);
    }

    private StaffAccount createAndInsertStaffAccount() {
        StaffAccount account = new StaffAccountFixture()
                .withCreatedBy(adminId)
                .build();
        jdbcHelper.insertStaffAccount(account);
        return account;
    }

    private void insertRefreshTokensFor(StaffAccount staffAccount) {
        RefreshToken token1 = new RefreshTokenFixture()
                .withStaffAccountId(staffAccount.getId().getValue().toString())
                .build();
        jdbcHelper.insertRefreshToken(token1);

        RefreshToken token2 = new RefreshTokenFixture()
                .withStaffAccountId(staffAccount.getId().getValue().toString())
                .build();
        jdbcHelper.insertRefreshToken(token2);
    }

    private void assertAllTokensRevoked(StaffAccount staffAccount) {
        List<RefreshToken> retrievedTokens = jdbcHelper.getAllRefreshTokensByStaffAccountId(staffAccount.getId());
        assertThat(retrievedTokens).allSatisfy(token -> {
            assertThat(token.isRevoked()).isTrue();
            assertThat(token.getRevokedAt()).isNotNull();
            assertThat(token.getVersion().getValue()).isGreaterThan(1);
        });
    }
}
