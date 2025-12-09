package com.paragon.integration;

import com.paragon.infrastructure.persistence.jdbc.transaction.UnitOfWorkAwareDataSource;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
public abstract class IntegrationTestBase {
    @Autowired protected MockMvc mockMvc;
    @Autowired private WebApplicationContext context;
    @Autowired private JdbcTemplate jdbc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired private UnitOfWorkAwareDataSource dataSource;

    protected final String adminId = "00000000-0000-0000-0000-000000000001";

    @AfterEach
    void cleanDynamicData() {
        rollbackAnyActiveTransactions();
        jdbc.execute("DELETE FROM staff_account_requests");
        jdbc.execute("DELETE FROM refresh_tokens");
        jdbc.execute("DELETE FROM audit_trail");
        jdbc.execute("DELETE FROM staff_account_password_history");
        jdbc.execute("DELETE FROM staff_accounts WHERE username != 'admin'");
    }

    private void rollbackAnyActiveTransactions() {
        if (dataSource.isTransactionActive()) {
            try {
                dataSource.rollbackTransaction();
            } catch (SQLException e) {
                // ignore
            }
        }
    }
}
