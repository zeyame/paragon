package com.paragon.integration.api;

import com.paragon.api.dtos.ErrorDto;
import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountRequestDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountResponseDto;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.RefreshTokenTests;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.TestPasswordHasherHelper;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthControllerTests {
    @Nested
    class Login extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;
        private WriteJdbcHelper writeJdbcHelper;

        @Autowired
        public Login(WriteJdbcHelper writeJdbcHelper) {
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldLoginStaffAccount() throws Exception {
            // Given
            String plaintextPassword = "SecurePass123!";
            String hashedPassword = TestPasswordHasherHelper.hash(plaintextPassword);

            StaffAccount staffAccountToLogin = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPassword(hashedPassword)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountToLogin);

            LoginStaffAccountRequestDto requestDto = createLoginRequest(
                    staffAccountToLogin.getUsername().getValue(),
                    plaintextPassword
            );

            // When
            MvcResult mvcResult = sendRequest(requestDto);

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            String responseBody = response.getContentAsString();
            ResponseDto<LoginStaffAccountResponseDto> responseDto = objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, LoginStaffAccountResponseDto.class));

            LoginStaffAccountResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNotNull();
            assertThat(errorDto).isNull();

            // assert response contains expected values
            assertThat(result.id()).isEqualTo(staffAccountToLogin.getId().getValue().toString());
            assertThat(result.username()).isEqualTo(requestDto.username());
            assertThat(result.requiresPasswordReset()).isEqualTo(staffAccountToLogin.requiresPasswordReset());
            assertThat(result.version()).isEqualTo(2);

            // assert jwt was set in the header
            String authorizationHeader = response.getHeader("Authorization");
            assertThat(authorizationHeader).isNotBlank();
            assertThat(authorizationHeader).startsWith("Bearer ");

            // assert refresh token was set in the cookie
            Cookie refreshTokenCookie = response.getCookie("refresh_token");
            assertThat(refreshTokenCookie).isNotNull();
            assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
            assertThat(refreshTokenCookie.getSecure()).isTrue();
            assertThat(refreshTokenCookie.getValue()).isNotBlank();
        }

        @Test
        void invalidUsername_shouldReturnUnauthorized() throws Exception {
            // Given
            StaffAccount staffAccountToLogin = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withUsername("Username123")
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountToLogin);

            LoginStaffAccountRequestDto requestDto = createLoginRequest(
                    "Incorrect_username",
                    staffAccountToLogin.getPassword().getValue()
            );

            // When
            MvcResult mvcResult = sendRequest(requestDto);

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

            String responseBody = response.getContentAsString();
            ResponseDto<LoginStaffAccountResponseDto> responseDto = objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, LoginStaffAccountResponseDto.class));

            LoginStaffAccountResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            assertThat(errorDto.message()).isEqualTo(AppExceptionInfo.invalidLoginCredentials().getMessage());
            assertThat(errorDto.code()).isEqualTo(AppExceptionInfo.invalidLoginCredentials().getAppErrorCode());
        }

        @Test
        void invalidPassword_shouldReturnUnauthorized() throws Exception {
            // Given
            String plaintextPassword = "SecurePass123!";
            String hashedPassword = TestPasswordHasherHelper.hash(plaintextPassword);

            StaffAccount staffAccountToLogin = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPassword(hashedPassword)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountToLogin);

            LoginStaffAccountRequestDto requestDto = createLoginRequest(
                    staffAccountToLogin.getUsername().getValue(),
                    "IncorrectPassword123"
            );

            // When
            MvcResult mvcResult = sendRequest(requestDto);

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

            String responseBody = response.getContentAsString();
            ResponseDto<LoginStaffAccountResponseDto> responseDto = objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, LoginStaffAccountResponseDto.class));

            LoginStaffAccountResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            assertThat(errorDto.message()).isEqualTo(StaffAccountExceptionInfo.invalidCredentials().getMessage());
            assertThat(errorDto.code()).isEqualTo(StaffAccountExceptionInfo.invalidCredentials().getDomainErrorCode());
        }

        @Test
        void accountDisabled_shouldReturnUnauthorized() throws Exception {
            // Given
            String plaintextPassword = "SecurePass123!";
            String hashedPassword = TestPasswordHasherHelper.hash(plaintextPassword);

            StaffAccount staffAccountToLogin = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPassword(hashedPassword)
                    .withStatus(StaffAccountStatus.DISABLED)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountToLogin);

            LoginStaffAccountRequestDto requestDto = createLoginRequest(
                    staffAccountToLogin.getUsername().getValue(),
                    plaintextPassword
            );

            // When
            MvcResult mvcResult = sendRequest(requestDto);

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

            String responseBody = response.getContentAsString();
            ResponseDto<LoginStaffAccountResponseDto> responseDto = objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, LoginStaffAccountResponseDto.class));

            LoginStaffAccountResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            assertThat(errorDto.message()).isEqualTo(StaffAccountExceptionInfo.loginFailedAccountDisabled().getMessage());
            assertThat(errorDto.code()).isEqualTo(StaffAccountExceptionInfo.loginFailedAccountDisabled().getDomainErrorCode());
        }

        @Test
        void accountLocked_shouldReturnUnauthorized() throws Exception {
            // Given
            String plaintextPassword = "SecurePass123!";
            String hashedPassword = TestPasswordHasherHelper.hash(plaintextPassword);

            StaffAccount staffAccountToLogin = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPassword(hashedPassword)
                    .withStatus(StaffAccountStatus.LOCKED)
                    .withLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES))
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountToLogin);

            LoginStaffAccountRequestDto requestDto = createLoginRequest(
                    staffAccountToLogin.getUsername().getValue(),
                    plaintextPassword
            );

            // When
            MvcResult mvcResult = sendRequest(requestDto);

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

            String responseBody = response.getContentAsString();
            ResponseDto<LoginStaffAccountResponseDto> responseDto = objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, LoginStaffAccountResponseDto.class));

            LoginStaffAccountResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            assertThat(errorDto.message()).isEqualTo(StaffAccountExceptionInfo.loginFailedAccountLocked().getMessage());
            assertThat(errorDto.code()).isEqualTo(StaffAccountExceptionInfo.loginFailedAccountLocked().getDomainErrorCode());
        }

        private LoginStaffAccountRequestDto createLoginRequest(String username, String password) {
            return new LoginStaffAccountRequestDto(
                    username,
                    password
            );
        }

        private MvcResult sendRequest(LoginStaffAccountRequestDto requestDto) throws Exception {
            MvcResult mvcResult = mockMvc.perform(
                    post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
            ).andReturn();
            return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
        }
    }
}
