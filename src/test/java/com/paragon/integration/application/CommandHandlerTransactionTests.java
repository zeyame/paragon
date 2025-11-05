package com.paragon.integration.application;

import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommand;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandHandler;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.TestPasswordHasherHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CommandHandlerTransactionTests {
    @Nested
    class RegisterStaffAccount extends IntegrationTestBase {
        private final RegisterStaffAccountCommandHandler handler;
        private final WriteJdbcHelper writeJdbcHelper;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public RegisterStaffAccount(RegisterStaffAccountCommandHandler handler, WriteJdbcHelper writeJdbcHelper) {
            this.handler = handler;
            this.writeJdbcHelper = writeJdbcHelper;
            this.jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldRollbackTransaction_whenExceptionOccursDuringRegistration() {
            // Given
            StaffAccount existingAccount = new StaffAccountFixture()
                    .withUsername("Username123")
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(existingAccount);

            RegisterStaffAccountCommand command = new RegisterStaffAccountCommand(
                    "Username123", // duplicate username
                    "new-user@email.com",
                    "TempPass123!",
                    10,
                    20,
                    List.of(SystemPermissions.VIEW_ACCOUNTS_LIST.getValue()),
                    adminId
            );

            // When
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(AppException.class);

            // Then
            Optional<StaffAccount> result = jdbcHelper.getStaffAccountByUsername(Username.of("Username123"));
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(existingAccount.getId());
        }
    }

    @Nested
    class LoginStaffAccount extends IntegrationTestBase {
        private final LoginStaffAccountCommandHandler handler;
        private final WriteJdbcHelper writeJdbcHelper;
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public LoginStaffAccount(LoginStaffAccountCommandHandler handler, WriteJdbcHelper writeJdbcHelper) {
            this.handler = handler;
            this.writeJdbcHelper = writeJdbcHelper;
            this.jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldRollbackTransaction_whenRefreshTokenCreationFails() {
            // Given: Setup staff account with correct password
            String plaintextPassword = "SecurePass123!";
            String hashedPassword = TestPasswordHasherHelper.hash(plaintextPassword);

            StaffAccount existingAccount = new StaffAccountFixture()
                    .withUsername("LoginUser123")
                    .withPassword(hashedPassword)
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(existingAccount);

            LoginStaffAccountCommand command = new LoginStaffAccountCommand(
                    "LoginUser123",
                    plaintextPassword,
                    "invalid-ip-format"  // forces IpAddress.of() to throw DomainException
            );

            // When
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(AppException.class);

            // Then
            Optional<StaffAccount> result = jdbcHelper.getStaffAccountByUsername(Username.of("LoginUser123"));
            assertThat(result).isPresent();

            StaffAccount accountAfterRollback = result.get();
            assertThat(accountAfterRollback.getLastLoginAt()).isNull();
            assertThat(accountAfterRollback.getVersion().getValue()).isEqualTo(1);
            assertThat(accountAfterRollback.getFailedLoginAttempts().getValue()).isEqualTo(0);
        }
    }
}
