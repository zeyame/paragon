package com.paragon.integration.api;

import com.paragon.api.dtos.ErrorDto;
import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.disable.DisableStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.enable.EnableStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.getall.StaffAccountSummaryResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.resetpassword.ResetStaffAccountPasswordResponseDto;
import com.paragon.api.exceptions.PermissionDeniedException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.exceptions.aggregate.StaffAccountExceptionInfo;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.Username;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.TestJwtHelper;
import com.paragon.helpers.TestPasswordHasherHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class StaffAccountControllerTests {
    @Nested
    class Register extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;
        private final List<PermissionCode> adminPermissions;

        @Autowired
        public Register(WriteJdbcHelper writeJdbcHelper) {
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
            adminPermissions = jdbcHelper.getPermissionsForStaff(StaffAccountId.from(adminId));
        }

        @Test
        void shouldRegisterStaffAccount() throws Exception {
            // Given
            RegisterStaffAccountRequestDto requestDto = createValidRequest();

            // When
            MvcResult result = sendAsyncRequest(requestDto, adminId, adminPermissions);

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            String responseBody = response.getContentAsString();
            ResponseDto<RegisterStaffAccountResponseDto> responseDto = objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, RegisterStaffAccountResponseDto.class));

            // verify that response is correct
            RegisterStaffAccountResponseDto resultBody = responseDto.result();

            assertThat(resultBody.username()).isEqualTo(requestDto.username());
            assertThat(resultBody.status()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE.toString());
            assertThat(resultBody.version()).isEqualTo(1);

            // verify that new account has been registered
            Optional<StaffAccount> optionalStaffAccount = jdbcHelper.getStaffAccountByUsername(Username.of(requestDto.username()));
            assertThat(optionalStaffAccount).isPresent();

            StaffAccount registeredAccount = optionalStaffAccount.get();
            assertThat(registeredAccount.getUsername().getValue()).isEqualTo(requestDto.username());
            assertThat(registeredAccount.getEmail().getValue()).isEqualTo(requestDto.email());
            assertThat(registeredAccount.getOrderAccessDuration().getValueInDays()).isEqualTo(requestDto.orderAccessDuration());
            assertThat(registeredAccount.getModmailTranscriptAccessDuration().getValueInDays()).isEqualTo(requestDto.modmailTranscriptAccessDuration());
            assertThat(registeredAccount.getPermissionCodes().stream().map(PermissionCode::getValue).toList()).isEqualTo(requestDto.permissionCodes());
        }

        @Test
        void shouldReturnForbiddenWhenRequestingStaffAccountLacksPermissions() throws Exception {
            // Given
            RegisterStaffAccountRequestDto requestDto = createValidRequest();

            StaffAccount staffAccountLackingPermissions = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of(SystemPermissions.VIEW_ACCOUNTS_LIST.getValue()))
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountLackingPermissions);

            // When
            MvcResult result = sendRequest(
                    requestDto,
                    staffAccountLackingPermissions.getId().getValue().toString(),
                    List.of(SystemPermissions.VIEW_ACCOUNTS_LIST)
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

            String responseBody = result.getResponse().getContentAsString();
            ResponseDto<RegisterStaffAccountResponseDto> responseDto = objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, RegisterStaffAccountResponseDto.class));

            RegisterStaffAccountResponseDto resultBody = responseDto.result();
            assertThat(resultBody).isNull();

            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            PermissionDeniedException expected = PermissionDeniedException.accessDenied();
            assertThat(errorDto.message()).isEqualTo(expected.getMessage());
            assertThat(errorDto.code()).isEqualTo(expected.getErrorCode());
        }

        @Test
        void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
            // Given
            RegisterStaffAccountRequestDto requestDto = createValidRequest();

            StaffAccount existing = new StaffAccountFixture()
                    .withUsername(requestDto.username())
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(existing);

            // When
            MvcResult result = sendAsyncRequest(requestDto, adminId, adminPermissions);

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

            String responseBody = response.getContentAsString();
            ResponseDto<RegisterStaffAccountResponseDto> responseDto =
                    objectMapper.readValue(responseBody,
                            objectMapper.getTypeFactory()
                                    .constructParametricType(ResponseDto.class, RegisterStaffAccountResponseDto.class));

            assertThat(responseDto.result()).isNull();

            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();
            assertThat(errorDto.message()).isEqualTo(AppExceptionInfo.staffAccountUsernameAlreadyExists(requestDto.username()).getMessage());
            assertThat(errorDto.code()).isEqualTo(AppExceptionInfo.staffAccountUsernameAlreadyExists(requestDto.username()).getAppErrorCode());
        }

        private RegisterStaffAccountRequestDto createValidRequest() {
            return new RegisterStaffAccountRequestDto(
                    "john_doe",
                    "john_doe@example.com",
                    10,
                    20,
                    List.of(SystemPermissions.VIEW_ACCOUNTS_LIST.getValue(), SystemPermissions.VIEW_BACKUP_LIST.getValue())
            );
        }

        private MvcResult sendRequest(RegisterStaffAccountRequestDto requestDto, String actorId, List<PermissionCode> permissionCodes) throws Exception {
            return mockMvc.perform(
                    post("/v1/staff-accounts")
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissionCodes))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
            ).andReturn();
        }

        private MvcResult sendAsyncRequest(RegisterStaffAccountRequestDto requestDto, String actorId, List<PermissionCode> permissionCodes) throws Exception {
            MvcResult mvcResult = mockMvc.perform(
                    post("/v1/staff-accounts")
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissionCodes))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
            ).andReturn();

            return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
        }
    }

    @Nested
    class Disable extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;
        private final List<PermissionCode> adminPermissions;

        @Autowired
        public Disable(WriteJdbcHelper writeJdbcHelper) {
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
            adminPermissions = jdbcHelper.getPermissionsForStaff(StaffAccountId.from(adminId));
        }

        @Test
        void shouldDisableStaffAccount() throws Exception {
            // Given
            StaffAccount staffAccountToDisable = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of(SystemPermissions.VIEW_ACCOUNTS_LIST.getValue()))
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountToDisable);

            // When
            MvcResult result = sendAsyncRequest(
                    staffAccountToDisable.getId().getValue().toString(),
                    adminId,
                    adminPermissions
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            String responseBody = response.getContentAsString();
            ResponseDto<DisableStaffAccountResponseDto> responseDto = objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, DisableStaffAccountResponseDto.class)
            );

            DisableStaffAccountResponseDto resultBody = responseDto.result();
            assertThat(resultBody).isNotNull();
            assertThat(responseDto.errorDto()).isNull();
            assertThat(resultBody.id()).isEqualTo(staffAccountToDisable.getId().getValue().toString());
            assertThat(resultBody.status()).isEqualTo(StaffAccountStatus.DISABLED.toString());
            assertThat(resultBody.disabledBy()).isEqualTo(adminId);

            Optional<StaffAccount> optionalStaffAccount = jdbcHelper.getStaffAccountById(staffAccountToDisable.getId());
            assertThat(optionalStaffAccount).isPresent();

            StaffAccount disabledAccount = optionalStaffAccount.get();
            assertThat(disabledAccount.getStatus()).isEqualTo(StaffAccountStatus.DISABLED);
            assertThat(disabledAccount.getDisabledBy()).isEqualTo(StaffAccountId.from(adminId));
            assertThat(disabledAccount.getVersion().getValue()).isEqualTo(2);
        }

        @Test
        void shouldReturnForbiddenWhenRequestingStaffAccountLacksPermission() throws Exception {
            // Given
            StaffAccount disablingStaffAccount = new StaffAccountFixture()
                    .withUsername("username1")
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of(SystemPermissions.VIEW_ACCOUNTS_LIST.getValue())) // lacks MANAGE_ACCOUNTS permission
                    .build();
            jdbcHelper.insertStaffAccount(disablingStaffAccount);

            StaffAccount staffAccountToDisable = new StaffAccountFixture()
                    .withUsername("username2")
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountToDisable);

            // When
            MvcResult result = sendRequest(
                    staffAccountToDisable.getId().getValue().toString(),
                    disablingStaffAccount.getId().getValue().toString(),
                    List.of(SystemPermissions.VIEW_ACCOUNTS_LIST)
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

            String responseBody = response.getContentAsString();
            ResponseDto<DisableStaffAccountResponseDto> responseDto = objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, DisableStaffAccountResponseDto.class)
            );

            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            // verify that error dto contains correct message and error code
            PermissionDeniedException expectedException = PermissionDeniedException.accessDenied();
            assertThat(errorDto.message()).isEqualTo(expectedException.getMessage());
            assertThat(errorDto.code()).isEqualTo(expectedException.getErrorCode());

            // verify that staff account to be disabled remain the same
            StaffAccount persistedAccount = jdbcHelper.getStaffAccountById(staffAccountToDisable.getId()).orElseThrow();
            assertThat(persistedAccount.getStatus()).isEqualTo(staffAccountToDisable.getStatus());
            assertThat(persistedAccount.getDisabledBy()).isNull();
            assertThat(persistedAccount.getVersion().getValue()).isEqualTo(staffAccountToDisable.getVersion().getValue());
        }

        @Test
        void shouldReturnConflictWhenStaffAccountIsAlreadyDisabled() throws Exception {
            // Given
            StaffAccount disabledAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withDisabledBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(disabledAccount);

            // When
            MvcResult result = sendAsyncRequest(
                    disabledAccount.getId().getValue().toString(),
                    adminId,
                    adminPermissions
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

            String responseBody = response.getContentAsString();
            ResponseDto<DisableStaffAccountResponseDto> responseDto = objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, DisableStaffAccountResponseDto.class)
            );

            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            // verify that the error dto has the expected message and error code
            var expectedInfo = StaffAccountExceptionInfo.accountAlreadyDisabled();
            assertThat(errorDto.message()).isEqualTo(expectedInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(expectedInfo.getDomainErrorCode());

            // verify that the state of the already disabled account has not changed
            StaffAccount persistedAccount = jdbcHelper.getStaffAccountById(disabledAccount.getId()).orElseThrow();
            assertThat(persistedAccount.getStatus()).isEqualTo(StaffAccountStatus.DISABLED);
            assertThat(persistedAccount.getDisabledBy()).isEqualTo(StaffAccountId.from(adminId));
            assertThat(persistedAccount.getVersion().getValue()).isEqualTo(disabledAccount.getVersion().getValue());
        }

        @Test
        void shouldReturnNotFoundWhenStaffAccountDoesNotExist() throws Exception {
            // Given
            String missingStaffAccountId = UUID.randomUUID().toString();

            // When
            MvcResult result = sendAsyncRequest(
                    missingStaffAccountId,
                    adminId,
                    adminPermissions
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

            String responseBody = response.getContentAsString();
            ResponseDto<DisableStaffAccountResponseDto> responseDto = objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, DisableStaffAccountResponseDto.class)
            );

            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            AppExceptionInfo expectedInfo = AppExceptionInfo.staffAccountNotFound(missingStaffAccountId);
            assertThat(errorDto.message()).isEqualTo(expectedInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(expectedInfo.getAppErrorCode());
        }

        private MvcResult sendRequest(String staffAccountId, String actorId, List<PermissionCode> permissions) throws Exception {
            return mockMvc.perform(
                    put("/v1/staff-accounts/disable/" + staffAccountId)
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
            ).andReturn();
        }

        private MvcResult sendAsyncRequest(String staffAccountId, String actorId, List<PermissionCode> permissions) throws Exception {
            MvcResult mvcResult = mockMvc.perform(
                    put("/v1/staff-accounts/disable/" + staffAccountId)
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
            ).andReturn();

            return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
        }
    }

    @Nested
    class Enable extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;
        private final List<PermissionCode> adminPermissions;

        @Autowired
        public Enable(WriteJdbcHelper writeJdbcHelper) {
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
            adminPermissions = jdbcHelper.getPermissionsForStaff(StaffAccountId.from(adminId));
        }

        @Test
        void shouldEnableStaffAccount() throws Exception {
            // Given
            StaffAccount staffAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withPasswordTemporary(false)
                    .withDisabledBy(adminId)
                    .withFailedLoginAttempts(4)
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount);

            // When
            MvcResult result = sendAsyncRequest(
                    staffAccount.getId().getValue().toString(),
                    adminId,
                    adminPermissions
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            ResponseDto<EnableStaffAccountResponseDto> responseDto = objectMapper.readValue(
                    response.getContentAsString(),
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, EnableStaffAccountResponseDto.class)
            );

            // verify the correct response dto was returned
            EnableStaffAccountResponseDto resultBody = responseDto.result();
            assertThat(resultBody).isNotNull();
            assertThat(resultBody.id()).isEqualTo(staffAccount.getId().getValue().toString());
            assertThat(resultBody.status()).isEqualTo(StaffAccountStatus.ACTIVE.toString());
            assertThat(resultBody.enabledBy()).isEqualTo(adminId);
            assertThat(resultBody.version()).isEqualTo(staffAccount.getVersion().getValue() + 1);

            // verify the disabled account was enabled
            StaffAccount enabledAccount = jdbcHelper.getStaffAccountById(staffAccount.getId()).orElseThrow();
            assertThat(enabledAccount.getStatus()).isEqualTo(StaffAccountStatus.ACTIVE);
            assertThat(enabledAccount.getDisabledBy()).isNull();
            assertThat(enabledAccount.getEnabledBy()).isEqualTo(StaffAccountId.from(adminId));
            assertThat(enabledAccount.getFailedLoginAttempts().getValue()).isZero();
            assertThat(enabledAccount.getVersion().getValue()).isEqualTo(staffAccount.getVersion().getValue() + 1);
        }

        @Test
        void shouldReturnForbiddenWhenRequestingStaffAccountLacksPermission() throws Exception {
            // Given
            StaffAccount enablingStaffAccount = new StaffAccountFixture()
                    .withUsername("enabling_account")
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of(SystemPermissions.VIEW_ACCOUNTS_LIST.getValue())) // lacks required permissions
                    .build();
            jdbcHelper.insertStaffAccount(enablingStaffAccount);

            StaffAccount disabledAccount = new StaffAccountFixture()
                    .withUsername("disabled_account")
                    .withCreatedBy(adminId)
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withDisabledBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(disabledAccount);

            // When
            MvcResult result = sendRequest(
                    disabledAccount.getId().getValue().toString(),
                    enablingStaffAccount.getId().getValue().toString(),
                    List.of(SystemPermissions.VIEW_ACCOUNTS_LIST)
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

            ResponseDto<EnableStaffAccountResponseDto> responseDto = objectMapper.readValue(
                    response.getContentAsString(),
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, EnableStaffAccountResponseDto.class)
            );

            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            // verify error dto contains the expected message and error code
            PermissionDeniedException expected = PermissionDeniedException.accessDenied();
            assertThat(errorDto.message()).isEqualTo(expected.getMessage());
            assertThat(errorDto.code()).isEqualTo(expected.getErrorCode());

            // verify disabled account remained disabled
            StaffAccount persistedAccount = jdbcHelper.getStaffAccountById(disabledAccount.getId()).orElseThrow();
            assertThat(persistedAccount.getStatus()).isEqualTo(StaffAccountStatus.DISABLED);
            assertThat(persistedAccount.getDisabledBy()).isEqualTo(StaffAccountId.from(adminId));
            assertThat(persistedAccount.getEnabledBy()).isNull();
        }

        @Test
        void shouldReturnConflictWhenStaffAccountIsAlreadyEnabled() throws Exception {
            // Given
            StaffAccount activeAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .withDisabledBy(null)
                    .build();
            jdbcHelper.insertStaffAccount(activeAccount);

            // When
            MvcResult result = sendAsyncRequest(
                    activeAccount.getId().getValue().toString(),
                    adminId,
                    adminPermissions
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

            ResponseDto<EnableStaffAccountResponseDto> responseDto = objectMapper.readValue(
                    response.getContentAsString(),
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, EnableStaffAccountResponseDto.class)
            );

            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            // verify error dto contains expected message and error code
            var expectedInfo = StaffAccountExceptionInfo.accountAlreadyEnabled();
            assertThat(errorDto.message()).isEqualTo(expectedInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(expectedInfo.getDomainErrorCode());

            // verify that account state remained unchanged
            StaffAccount persistedAccount = jdbcHelper.getStaffAccountById(activeAccount.getId()).orElseThrow();
            assertThat(persistedAccount.getStatus()).isEqualTo(StaffAccountStatus.ACTIVE);
            assertThat(persistedAccount.getEnabledBy()).isNull();
        }

        @Test
        void shouldReturnNotFoundWhenStaffAccountDoesNotExist() throws Exception {
            // Given
            String missingStaffAccountId = UUID.randomUUID().toString();

            // When
            MvcResult result = sendAsyncRequest(
                    missingStaffAccountId,
                    adminId,
                    adminPermissions
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

            ResponseDto<EnableStaffAccountResponseDto> responseDto = objectMapper.readValue(
                    response.getContentAsString(),
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, EnableStaffAccountResponseDto.class)
            );

            // verify that the error dto contains the expected error code and message
            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            AppExceptionInfo expectedInfo = AppExceptionInfo.staffAccountNotFound(missingStaffAccountId);
            assertThat(errorDto.message()).isEqualTo(expectedInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(expectedInfo.getAppErrorCode());
        }

        private MvcResult sendRequest(String staffAccountId, String actorId, List<PermissionCode> permissions) throws Exception {
            return mockMvc.perform(
                    put("/v1/staff-accounts/enable/" + staffAccountId)
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
            ).andReturn();
        }

        private MvcResult sendAsyncRequest(String staffAccountId, String actorId, List<PermissionCode> permissions) throws Exception {
            MvcResult mvcResult = mockMvc.perform(
                    put("/v1/staff-accounts/enable/" + staffAccountId)
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
            ).andReturn();

            return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
        }
    }

    @Nested
    class ResetPassword extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;
        private final List<PermissionCode> adminPermissions;

        @Autowired
        public ResetPassword(WriteJdbcHelper writeJdbcHelper) {
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
            adminPermissions = jdbcHelper.getPermissionsForStaff(StaffAccountId.from(adminId));
        }

        @Test
        void shouldResetPassword() throws Exception {
            // Given
            StaffAccount targetAccount = new StaffAccountFixture()
                    .withUsername("target_account")
                    .withCreatedBy(adminId)
                    .withPassword("old-password")
                    .withPasswordTemporary(false)
                    .withPasswordIssuedAt(Instant.now().minus(120, ChronoUnit.SECONDS))
                    .withStatus(StaffAccountStatus.ACTIVE)
                    .build();
            jdbcHelper.insertStaffAccount(targetAccount);

            // When
            MvcResult result = sendAsyncRequest(
                    targetAccount.getId().getValue().toString(),
                    adminId,
                    adminPermissions
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            String responseBody = response.getContentAsString();
            ResponseDto<ResetStaffAccountPasswordResponseDto> responseDto = objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, ResetStaffAccountPasswordResponseDto.class)
            );

            // verify response is correct
            ResetStaffAccountPasswordResponseDto resultBody = responseDto.result();
            assertThat(resultBody).isNotNull();
            assertThat(resultBody.id()).isEqualTo(targetAccount.getId().getValue().toString());
            assertThat(resultBody.status()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE.toString());
            assertThat(resultBody.temporaryPassword()).isNotBlank();
            assertThat(resultBody.passwordIssuedAt()).isNotNull();
            assertThat(resultBody.version()).isGreaterThan(targetAccount.getVersion().getValue());

            // verify account password has been reset
            StaffAccount persistedAccount = jdbcHelper.getStaffAccountById(targetAccount.getId()).orElseThrow();
            assertThat(persistedAccount.getStatus()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE);
            assertThat(persistedAccount.isPasswordTemporary()).isTrue();
            assertThat(persistedAccount.getPassword().getValue()).isNotEqualTo(targetAccount.getPassword().getValue());
            assertThat(persistedAccount.getPasswordIssuedAt()).isNotEqualTo(targetAccount.getPasswordIssuedAt());
        }

        @Test
        void shouldReturnForbiddenWhenRequestingStaffAccountLacksPermission() throws Exception {
            // Given
            StaffAccount resettingStaffAccount = new StaffAccountFixture()
                    .withUsername("no_reset_permission")
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of(SystemPermissions.VIEW_ACCOUNTS_LIST.getValue()))
                    .build();
            jdbcHelper.insertStaffAccount(resettingStaffAccount);

            StaffAccount targetAccount = new StaffAccountFixture()
                    .withUsername("target_account")
                    .withCreatedBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(targetAccount);

            // When
            MvcResult result = sendRequest(
                    targetAccount.getId().getValue().toString(),
                    resettingStaffAccount.getId().getValue().toString(),
                    List.of(SystemPermissions.VIEW_ACCOUNTS_LIST)
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

            String responseBody = response.getContentAsString();
            ResponseDto<ResetStaffAccountPasswordResponseDto> responseDto = objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, ResetStaffAccountPasswordResponseDto.class)
            );

            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            PermissionDeniedException expected = PermissionDeniedException.accessDenied();
            assertThat(errorDto.message()).isEqualTo(expected.getMessage());
            assertThat(errorDto.code()).isEqualTo(expected.getErrorCode());

            StaffAccount persistedAccount = jdbcHelper.getStaffAccountById(targetAccount.getId()).orElseThrow();
            assertThat(persistedAccount.getPassword().getValue()).isEqualTo(targetAccount.getPassword().getValue());
            assertThat(persistedAccount.getStatus()).isEqualTo(targetAccount.getStatus());
            assertThat(persistedAccount.getPasswordIssuedAt()).isEqualTo(targetAccount.getPasswordIssuedAt());
        }

        @Test
        void shouldReturnConflictWhenStaffAccountIsDisabled() throws Exception {
            // Given
            StaffAccount disabledAccount = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withStatus(StaffAccountStatus.DISABLED)
                    .withDisabledBy(adminId)
                    .build();
            jdbcHelper.insertStaffAccount(disabledAccount);

            // When
            MvcResult result = sendAsyncRequest(
                    disabledAccount.getId().getValue().toString(),
                    adminId,
                    adminPermissions
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

            String responseBody = response.getContentAsString();
            ResponseDto<ResetStaffAccountPasswordResponseDto> responseDto = objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, ResetStaffAccountPasswordResponseDto.class)
            );

            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            // verify error dto contains correct message and error code
            var expectedInfo = StaffAccountExceptionInfo.accountAlreadyDisabled();
            assertThat(errorDto.message()).isEqualTo(expectedInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(expectedInfo.getDomainErrorCode());

            // verify disabled account's state remained unchanged
            StaffAccount persistedAccount = jdbcHelper.getStaffAccountById(disabledAccount.getId()).orElseThrow();
            assertThat(persistedAccount.getStatus()).isEqualTo(StaffAccountStatus.DISABLED);
            assertThat(persistedAccount.getPassword().getValue()).isEqualTo(disabledAccount.getPassword().getValue());
            assertThat(persistedAccount.getPasswordIssuedAt()).isEqualTo(disabledAccount.getPasswordIssuedAt());
        }

        @Test
        void shouldReturnNotFoundWhenStaffAccountDoesNotExist() throws Exception {
            // Given
            String missingAccountId = UUID.randomUUID().toString();

            // When
            MvcResult result = sendAsyncRequest(
                    missingAccountId,
                    adminId,
                    adminPermissions
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

            String responseBody = response.getContentAsString();
            ResponseDto<ResetStaffAccountPasswordResponseDto> responseDto = objectMapper.readValue(
                    responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, ResetStaffAccountPasswordResponseDto.class)
            );

            assertThat(responseDto.result()).isNull();
            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            AppExceptionInfo expectedInfo = AppExceptionInfo.staffAccountNotFound(missingAccountId);
            assertThat(errorDto.message()).isEqualTo(expectedInfo.getMessage());
            assertThat(errorDto.code()).isEqualTo(expectedInfo.getAppErrorCode());
        }

        private MvcResult sendRequest(String staffAccountId, String actorId, List<PermissionCode> permissions) throws Exception {
            return mockMvc.perform(
                    put("/v1/staff-accounts/reset-password/" + staffAccountId)
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
            ).andReturn();
        }

        private MvcResult sendAsyncRequest(String staffAccountId, String actorId, List<PermissionCode> permissions) throws Exception {
            MvcResult mvcResult = mockMvc.perform(
                    put("/v1/staff-accounts/reset-password/" + staffAccountId)
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
            ).andReturn();

            return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
        }
    }

    @Nested
    class GetAll extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;
        private final List<PermissionCode> adminPermissions;
        
        @Autowired
        public GetAll(WriteJdbcHelper writeJdbcHelper) {
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
            adminPermissions = jdbcHelper.getPermissionsForStaff(StaffAccountId.from(adminId));
        }

        @Test
        void shouldReturnAllStaffAccounts() throws Exception {
            // Given
            StaffAccount staffAccount1 = new StaffAccountFixture()
                    .withUsername("john_doe")
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of(SystemPermissions.APPROVE_PASSWORD_CHANGE.getValue()))
                    .build();
            StaffAccount staffAccount2 = new StaffAccountFixture()
                    .withUsername("jane_smith")
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of(SystemPermissions.VIEW_LOGIN_LOGS.getValue()))
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount1);
            jdbcHelper.insertStaffAccount(staffAccount2);

            // When
            MvcResult result = sendAsyncRequest(adminId, adminPermissions);

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            String responseBody = response.getContentAsString();
            ResponseDto<GetAllStaffAccountsResponseDto> responseDto = objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, GetAllStaffAccountsResponseDto.class));

            GetAllStaffAccountsResponseDto resultBody = responseDto.result();
            assertThat(resultBody).isNotNull();
            assertThat(resultBody.staffAccountSummaryResponseDtos().size()).isEqualTo(3); // admin + 2 created accounts

            List<StaffAccountSummaryResponseDto> summaries = resultBody.staffAccountSummaryResponseDtos();
            assertThat(summaries).anySatisfy(summary -> {
                assertThat(summary.username()).isEqualTo("john_doe");
                assertThat(summary.status()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE.toString());
            });
            assertThat(summaries).anySatisfy(summary -> {
                assertThat(summary.username()).isEqualTo("jane_smith");
                assertThat(summary.status()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE.toString());
            });
        }

        @Test
        void shouldReturnForbiddenWhenRequestingStaffAccountLacksPermission() throws Exception {
            // Given
            StaffAccount staffAccountLackingPermission = new StaffAccountFixture()
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of())
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountLackingPermission);

            // When
            MvcResult result = sendRequest(
                staffAccountLackingPermission.getId().getValue().toString(),
                List.of()
            );

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

            String responseBody = response.getContentAsString();
            ResponseDto<GetAllStaffAccountsResponseDto> responseDto = objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, GetAllStaffAccountsResponseDto.class));

            assertThat(responseDto.result()).isNull();

            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();

            PermissionDeniedException expected = PermissionDeniedException.accessDenied();
            assertThat(errorDto.message()).isEqualTo(expected.getMessage());
            assertThat(errorDto.code()).isEqualTo(expected.getErrorCode());
        }

        private MvcResult sendRequest(String actorId, List<PermissionCode> permissions) throws Exception {
            return mockMvc.perform(
                    get("/v1/staff-accounts")
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
            ).andReturn();
        }

        private MvcResult sendAsyncRequest(String actorId, List<PermissionCode> permissions) throws Exception {
            MvcResult mvcResult = mockMvc.perform(
                    get("/v1/staff-accounts")
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
            ).andReturn();

            return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
        }
    }
}
