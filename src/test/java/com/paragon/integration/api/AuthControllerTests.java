package com.paragon.integration.api;

import com.paragon.api.dtos.ErrorDto;
import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.auth.completetemporarypassword.CompleteTemporaryPasswordRequestDto;
import com.paragon.api.dtos.auth.completetemporarypassword.CompleteTemporaryPasswordResponseDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountRequestDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountResponseDto;
import com.paragon.api.dtos.auth.refresh.RefreshStaffAccountTokenResponseDto;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.exceptions.valueobject.PlaintextRefreshTokenExceptionInfo;
import com.paragon.domain.models.aggregates.RefreshToken;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.valueobjects.DateTimeUtc;
import com.paragon.domain.models.valueobjects.PasswordHistoryEntry;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.TestPasswordHasherHelper;
import com.paragon.helpers.TestRefreshTokenHasherHelper;
import com.paragon.helpers.fixtures.PasswordHistoryEntryFixture;
import com.paragon.helpers.fixtures.RefreshTokenFixture;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class AuthControllerTests {
    @Nested
    class Login extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;

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

            assertThat(errorDto.message()).isEqualTo(AppExceptionInfo.invalidLoginCredentials().getMessage());
            assertThat(errorDto.code()).isEqualTo(AppExceptionInfo.invalidLoginCredentials().getAppErrorCode());
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

    @Nested
    class Refresh extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public Refresh(WriteJdbcHelper writeJdbcHelper) {
            this.jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnExpectedResponse() throws Exception {
            // Given
            StaffAccount staffAccount = persistStaffAccount();
            String existingPlainRefreshToken = "existing-refresh-token";

            RefreshToken refreshToken = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .withTokenHash(hashPlainRefreshToken(existingPlainRefreshToken))
                    .build();
            jdbcHelper.insertRefreshToken(refreshToken);

            // When
            MvcResult mvcResult = sendRequest(new Cookie("refresh_token", existingPlainRefreshToken));

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            ResponseDto<RefreshStaffAccountTokenResponseDto> responseDto = readResponse(response);
            RefreshStaffAccountTokenResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNotNull();
            assertThat(errorDto).isNull();
            assertThat(result.id()).isEqualTo(staffAccount.getId().getValue().toString());
            assertThat(result.username()).isEqualTo(staffAccount.getUsername().getValue());
            assertThat(result.requiresPasswordReset()).isEqualTo(staffAccount.requiresPasswordReset());
            assertThat(result.version()).isEqualTo(staffAccount.getVersion().getValue());
        }

        @Test
        void shouldSetNewJwtHeader() throws Exception {
            // Given
            StaffAccount staffAccount = persistStaffAccount();
            String existingPlainRefreshToken = "existing-refresh-token";

            RefreshToken refreshToken = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .withTokenHash(hashPlainRefreshToken(existingPlainRefreshToken))
                    .build();
            jdbcHelper.insertRefreshToken(refreshToken);

            // When
            MvcResult mvcResult = sendRequest(new Cookie("refresh_token", existingPlainRefreshToken));

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            String authorizationHeader = response.getHeader("Authorization");
            assertThat(authorizationHeader).isNotBlank();
            assertThat(authorizationHeader).startsWith("Bearer ");
        }

        @Test
        void shouldRotateRefreshTokenAndPersistNewCookie() throws Exception {
            // Given
            StaffAccount staffAccount = persistStaffAccount();
            String existingPlainRefreshToken = "existing-refresh-token";

            RefreshToken existingToken = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .withTokenHash(hashPlainRefreshToken(existingPlainRefreshToken))
                    .build();
            jdbcHelper.insertRefreshToken(existingToken);

            // When
            MvcResult mvcResult = sendRequest(new Cookie("refresh_token", existingPlainRefreshToken));

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            Cookie refreshTokenCookie = response.getCookie("refresh_token");
            assertThat(refreshTokenCookie).isNotNull();
            assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
            assertThat(refreshTokenCookie.getSecure()).isTrue();
            assertThat(refreshTokenCookie.getValue()).isNotBlank();
            assertThat(refreshTokenCookie.getValue()).isNotEqualTo(existingPlainRefreshToken);

            List<RefreshToken> refreshTokens = jdbcHelper.getAllRefreshTokensByStaffAccountId(staffAccount.getId());
            assertThat(refreshTokens).hasSize(2);

            RefreshToken revokedToken = refreshTokens.stream()
                    .filter(token -> token.getId().equals(existingToken.getId()))
                    .findFirst()
                    .orElseThrow();
            RefreshToken newToken = refreshTokens.stream()
                    .filter(token -> !token.getId().equals(existingToken.getId()))
                    .findFirst()
                    .orElseThrow();

            assertThat(revokedToken.isRevoked()).isTrue();
            assertThat(revokedToken.getReplacedBy()).isEqualTo(newToken.getId());

            String expectedNewHash = hashPlainRefreshToken(refreshTokenCookie.getValue());
            assertThat(newToken.isRevoked()).isFalse();
            assertThat(newToken.getTokenHash().getValue()).isEqualTo(expectedNewHash);
        }

        @Test
        void shouldReturnUnauthorized_whenRefreshTokenDoesNotExist() throws Exception {
            // Given
            String missingRefreshToken = "missing-refresh-token";

            // When
            MvcResult mvcResult = sendRequest(new Cookie("refresh_token", missingRefreshToken));

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

            ResponseDto<RefreshStaffAccountTokenResponseDto> responseDto = readResponse(response);
            RefreshStaffAccountTokenResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            AppExceptionInfo invalidRefreshTokenInfo = AppExceptionInfo.invalidRefreshToken();
            assertThat(errorDto.message()).isEqualTo(invalidRefreshTokenInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(invalidRefreshTokenInfo.getAppErrorCode());
        }

        @Test
        void shouldReturnUnauthorized_whenRefreshTokenIsExpired() throws Exception {
            // Given
            StaffAccount staffAccount = persistStaffAccount();
            String expiredPlainRefreshToken = "expired-refresh-token";

            RefreshToken expiredToken = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .withTokenHash(hashPlainRefreshToken(expiredPlainRefreshToken))
                    .withExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                    .build();
            jdbcHelper.insertRefreshToken(expiredToken);

            // When
            MvcResult mvcResult = sendRequest(new Cookie("refresh_token", expiredPlainRefreshToken));

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

            ResponseDto<RefreshStaffAccountTokenResponseDto> responseDto = readResponse(response);
            RefreshStaffAccountTokenResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            AppExceptionInfo invalidRefreshTokenInfo = AppExceptionInfo.invalidRefreshToken();
            assertThat(errorDto.message()).isEqualTo(invalidRefreshTokenInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(invalidRefreshTokenInfo.getAppErrorCode());

            List<RefreshToken> refreshTokens = jdbcHelper.getAllRefreshTokensByStaffAccountId(staffAccount.getId());
            assertThat(refreshTokens).hasSize(1);
            assertThat(refreshTokens.getFirst().isRevoked()).isFalse();
        }

        @Test
        void revokedRefreshToken_shouldReturnUnauthorizedAndRevokeAllActiveTokens() throws Exception {
            // Given
            StaffAccount staffAccount = persistStaffAccount();
            String revokedPlainRefreshToken = "revoked-refresh-token";
            String otherPlainRefreshToken = "active-refresh-token";

            RefreshToken revokedToken = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .withTokenHash(hashPlainRefreshToken(revokedPlainRefreshToken))
                    .withRevoked(true)
                    .withRevokedAt(Instant.now().minus(1, ChronoUnit.HOURS))
                    .build();
            jdbcHelper.insertRefreshToken(revokedToken);

            RefreshToken otherActiveToken = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .withTokenHash(hashPlainRefreshToken(otherPlainRefreshToken))
                    .build();
            jdbcHelper.insertRefreshToken(otherActiveToken);

            // When
            MvcResult mvcResult = sendRequest(new Cookie("refresh_token", revokedPlainRefreshToken));

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

            ResponseDto<RefreshStaffAccountTokenResponseDto> responseDto = readResponse(response);
            RefreshStaffAccountTokenResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            AppExceptionInfo invalidRefreshTokenInfo = AppExceptionInfo.invalidRefreshToken();
            assertThat(errorDto.message()).isEqualTo(invalidRefreshTokenInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(invalidRefreshTokenInfo.getAppErrorCode());

            List<RefreshToken> refreshTokens = jdbcHelper.getAllRefreshTokensByStaffAccountId(staffAccount.getId());
            assertThat(refreshTokens).hasSize(2);
            assertThat(refreshTokens).allMatch(RefreshToken::isRevoked);
        }

        private StaffAccount persistStaffAccount() {
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);
            return staffAccount;
        }

        private String hashPlainRefreshToken(String plainRefreshToken) {
            return TestRefreshTokenHasherHelper.hash(plainRefreshToken);
        }

        private ResponseDto<RefreshStaffAccountTokenResponseDto> readResponse(MockHttpServletResponse response) throws Exception {
            String responseBody = response.getContentAsString();
            return objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, RefreshStaffAccountTokenResponseDto.class)
            );
        }

        private MvcResult sendRequest(Cookie... cookies) throws Exception {
            MvcResult mvcResult = mockMvc.perform(
                    post("/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(cookies)
            ).andReturn();
            return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
        }
    }

    @Nested
    class Logout extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public Logout(WriteJdbcHelper writeJdbcHelper) {
            this.jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldLogoutAndRevokeRefreshToken() throws Exception {
            // Given
            StaffAccount staffAccount = persistStaffAccount();
            String plainRefreshToken = "logout-refresh-token";

            RefreshToken refreshToken = new RefreshTokenFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .withTokenHash(hashPlainRefreshToken(plainRefreshToken))
                    .build();
            jdbcHelper.insertRefreshToken(refreshToken);

            // When
            MvcResult mvcResult = sendRequest(new Cookie("refresh_token", plainRefreshToken));

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            // assert valid response (null)
            ResponseDto<Object> responseDto = readResponse(response);
            assertThat(responseDto.result()).isNull();
            assertThat(responseDto.errorDto()).isNull();

            // assert refresh token was revoked
            RefreshToken persistedToken = jdbcHelper.getRefreshTokenById(refreshToken.getId())
                    .orElseThrow();
            assertThat(persistedToken.isRevoked()).isTrue();
            assertThat(persistedToken.getRevokedAt()).isNotNull();

            // assert cookie is empty of token
            Cookie clearedCookie = response.getCookie("refresh_token");
            assertThat(clearedCookie).isNotNull();
            assertThat(clearedCookie.getValue()).isEmpty();
            assertThat(clearedCookie.getMaxAge()).isZero();
            assertThat(clearedCookie.isHttpOnly()).isTrue();
            assertThat(clearedCookie.getSecure()).isTrue();
        }

        @Test
        void shouldReturnUnauthorized_whenRefreshTokenDoesNotExist() throws Exception {
            // When
            MvcResult mvcResult = sendRequest(new Cookie("refresh_token", "missing-refresh-token"));

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

            ResponseDto<Object> responseDto = readResponse(response);
            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            AppExceptionInfo invalidRefreshTokenInfo = AppExceptionInfo.invalidRefreshToken();
            assertThat(errorDto.message()).isEqualTo(invalidRefreshTokenInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(invalidRefreshTokenInfo.getAppErrorCode());
        }

        @Test
        void shouldReturnBadRequest_whenRefreshTokenCookieMissing() throws Exception {
            // When
            MvcResult mvcResult = sendRequest();

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

            ResponseDto<Object> responseDto = readResponse(response);
            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            var missingValueInfo = PlaintextRefreshTokenExceptionInfo.missingValue();
            assertThat(errorDto.message()).isEqualTo(missingValueInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(missingValueInfo.getDomainErrorCode());
        }

        private StaffAccount persistStaffAccount() {
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);
            return staffAccount;
        }

        private ResponseDto<Object> readResponse(MockHttpServletResponse response) throws Exception {
            String responseBody = response.getContentAsString();
            return objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, Object.class)
            );
        }

        private MvcResult sendRequest(Cookie... cookies) throws Exception {
            var requestBuilder = post("/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON);

            if (cookies != null && cookies.length > 0) {
                requestBuilder.cookie(cookies);
            }

            MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
            return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
        }

        private String hashPlainRefreshToken(String plainRefreshToken) {
            return TestRefreshTokenHasherHelper.hash(plainRefreshToken);
        }
    }

    @Nested
    class CompleteTemporaryPassword extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public CompleteTemporaryPassword(WriteJdbcHelper writeJdbcHelper) {
            this.jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnOkWithExpectedResponse() throws Exception {
            // Given
            String plaintextPassword = "TemporaryPass123!";
            String hashedPassword = TestPasswordHasherHelper.hash(plaintextPassword);

            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPassword(hashedPassword)
                    .withStatus(StaffAccountStatus.PENDING_PASSWORD_CHANGE)
                    .withPasswordTemporary(true)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            PasswordHistoryEntry initialPasswordEntry = new PasswordHistoryEntryFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .withHashedPassword(staffAccount.getPassword().getValue())
                    .withTemporary(true)
                    .withChangedAt(Instant.now())
                    .build();
            jdbcHelper.insertPasswordHistoryEntry(initialPasswordEntry);

            String newPassword = "NewSecurePassword123!";
            CompleteTemporaryPasswordRequestDto requestDto = new CompleteTemporaryPasswordRequestDto(
                    staffAccount.getId().getValue().toString(),
                    newPassword
            );

            // When
            MvcResult mvcResult = sendRequest(requestDto);

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            ResponseDto<CompleteTemporaryPasswordResponseDto> responseDto = readResponse(response);
            CompleteTemporaryPasswordResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNotNull();
            assertThat(errorDto).isNull();
            assertThat(result.id()).isEqualTo(staffAccount.getId().getValue().toString());
            assertThat(result.username()).isEqualTo(staffAccount.getUsername().getValue());
            assertThat(result.status()).isEqualTo("ACTIVE");
            assertThat(result.version()).isEqualTo(2);
        }

        @Test
        void shouldReturnNotFound_whenStaffAccountDoesNotExist() throws Exception {
            // Given
            String nonExistentId = "00000000-0000-0000-0000-000000000999";
            CompleteTemporaryPasswordRequestDto requestDto = new CompleteTemporaryPasswordRequestDto(
                    nonExistentId,
                    "NewSecurePassword123!"
            );

            // When
            MvcResult mvcResult = sendRequest(requestDto);

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

            ResponseDto<CompleteTemporaryPasswordResponseDto> responseDto = readResponse(response);
            CompleteTemporaryPasswordResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            AppExceptionInfo staffAccountNotFoundInfo = AppExceptionInfo.staffAccountNotFound(nonExistentId);
            assertThat(errorDto.message()).isEqualTo(staffAccountNotFoundInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(staffAccountNotFoundInfo.getAppErrorCode());
        }

        @Test
        void shouldReturnConflict_whenPasswordMatchesCurrentPassword() throws Exception {
            // Given
            String plaintextPassword = "CurrentPassword123!";
            String hashedPassword = TestPasswordHasherHelper.hash(plaintextPassword);

            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPassword(hashedPassword)
                    .withStatus(StaffAccountStatus.PENDING_PASSWORD_CHANGE)
                    .withPasswordTemporary(true)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            // Insert initial password history entry
            PasswordHistoryEntry initialPasswordEntry = new PasswordHistoryEntryFixture()
                    .withStaffAccountId(staffAccount.getId().getValue().toString())
                    .withHashedPassword(staffAccount.getPassword().getValue())
                    .withTemporary(true)
                    .withChangedAt(Instant.now())
                    .build();
            jdbcHelper.insertPasswordHistoryEntry(initialPasswordEntry);

            CompleteTemporaryPasswordRequestDto requestDto = new CompleteTemporaryPasswordRequestDto(
                    staffAccount.getId().getValue().toString(),
                    plaintextPassword  // Same as current password
            );

            // When
            MvcResult mvcResult = sendRequest(requestDto);

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

            ResponseDto<CompleteTemporaryPasswordResponseDto> responseDto = readResponse(response);
            CompleteTemporaryPasswordResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            AppExceptionInfo newPasswordMatchesCurrentInfo = AppExceptionInfo.newPasswordMatchesCurrentPassword();
            assertThat(errorDto.message()).isEqualTo(newPasswordMatchesCurrentInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(newPasswordMatchesCurrentInfo.getAppErrorCode());
        }

        @Test
        void shouldReturnConflict_whenAccountStatusIsNotPending() throws Exception {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withStatus(StaffAccountStatus.ACTIVE)  // Already active
                    .withPasswordTemporary(false)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            // Insert initial password history entry
            PasswordHistoryEntry initialPasswordEntry = new PasswordHistoryEntry(
                    staffAccount.getId(),
                    staffAccount.getPassword(),
                    false,
                    DateTimeUtc.now()
            );
            jdbcHelper.insertPasswordHistoryEntry(initialPasswordEntry);

            CompleteTemporaryPasswordRequestDto requestDto = new CompleteTemporaryPasswordRequestDto(
                    staffAccount.getId().getValue().toString(),
                    "NewSecurePassword123!"
            );

            // When
            MvcResult mvcResult = sendRequest(requestDto);

            // Then
            MockHttpServletResponse response = mvcResult.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

            ResponseDto<CompleteTemporaryPasswordResponseDto> responseDto = readResponse(response);
            CompleteTemporaryPasswordResponseDto result = responseDto.result();
            ErrorDto errorDto = responseDto.errorDto();

            assertThat(result).isNull();
            assertThat(errorDto).isNotNull();

            StaffAccountExceptionInfo temporaryPasswordChangeRequiresPendingStateInfo =
                    StaffAccountExceptionInfo.temporaryPasswordChangeRequiresPendingState();
            assertThat(errorDto.message()).isEqualTo(temporaryPasswordChangeRequiresPendingStateInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(temporaryPasswordChangeRequiresPendingStateInfo.getDomainErrorCode());
        }

        private ResponseDto<CompleteTemporaryPasswordResponseDto> readResponse(MockHttpServletResponse response) throws Exception {
            String responseBody = response.getContentAsString();
            return objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, CompleteTemporaryPasswordResponseDto.class)
            );
        }

        private MvcResult sendRequest(CompleteTemporaryPasswordRequestDto requestDto) throws Exception {
            MvcResult mvcResult = mockMvc.perform(
                    put("/v1/auth/complete-temporary-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
            ).andReturn();
            return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
        }
    }
}
