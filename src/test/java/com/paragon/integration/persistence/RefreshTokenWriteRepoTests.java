package com.paragon.integration.persistence;

import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.infrastructure.persistence.repos.RefreshTokenWriteRepoImpl;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenWriteRepoTests {
    @Nested
    class Create extends IntegrationTestBase {
        private final RefreshTokenWriteRepoImpl sut;
        private WriteJdbcHelper writeJdbcHelper;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public Create(WriteJdbcHelper writeJdbcHelper) {
            sut = new RefreshTokenWriteRepoImpl(writeJdbcHelper);
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldCreateNewRefreshToken() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            RefreshToken insertedRefreshToken = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .build();

            // When
            sut.create(insertedRefreshToken);

            // Then
            Optional<RefreshToken> optionalRefreshToken = jdbcHelper.getRefreshTokenById(insertedRefreshToken.getId());
            assertThat(optionalRefreshToken).isPresent();

            RefreshToken retrievedRefreshToken = optionalRefreshToken.get();
            assertThat(retrievedRefreshToken)
                    .usingRecursiveComparison()
                    .isEqualTo(insertedRefreshToken);
        }
    }

    @Nested
    class GetActiveTokensByStaffAccountId extends IntegrationTestBase {
        private final RefreshTokenWriteRepoImpl sut;
        private WriteJdbcHelper writeJdbcHelper;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public GetActiveTokensByStaffAccountId(WriteJdbcHelper writeJdbcHelper) {
            sut = new RefreshTokenWriteRepoImpl(writeJdbcHelper);
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldGetActiveTokensByStaffAccountId() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            RefreshToken refreshToken1 = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .build();
            RefreshToken refreshToken2 = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .build();

            jdbcHelper.insertRefreshToken(refreshToken1);
            jdbcHelper.insertRefreshToken(refreshToken2);

            // When
            List<RefreshToken> refreshTokens = sut.getActiveTokensByStaffAccountId(staffAccount.getId());

            // Then
            assertThat(refreshTokens.size()).isEqualTo(2);
            assertThat(refreshTokens).containsExactlyInAnyOrder(refreshToken1, refreshToken2);
        }
    }

    @Nested
    class UpdateAll extends IntegrationTestBase {
        private final RefreshTokenWriteRepoImpl sut;
        private WriteJdbcHelper writeJdbcHelper;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public UpdateAll(WriteJdbcHelper writeJdbcHelper) {
            sut = new RefreshTokenWriteRepoImpl(writeJdbcHelper);
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldUpdateAllRefreshTokens() {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            RefreshToken refreshToken1 = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .build();
            RefreshToken refreshToken2 = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .build();

            jdbcHelper.insertRefreshToken(refreshToken1);
            jdbcHelper.insertRefreshToken(refreshToken2);

            // When
            refreshToken1.revoke();
            refreshToken2.revoke();
            sut.updateAll(List.of(refreshToken1, refreshToken2));

            // Then
            List<RefreshToken> updatedTokens = jdbcHelper.getAllRefreshTokensByStaffAccountId(staffAccount.getId());
            assertThat(updatedTokens).allSatisfy(token -> {
                assertThat(token.isRevoked()).isTrue();
                assertThat(token.getRevokedAt()).isNotNull();
                assertThat(token.getVersion().getValue()).isGreaterThan(1);
            });
        }
    }
}
