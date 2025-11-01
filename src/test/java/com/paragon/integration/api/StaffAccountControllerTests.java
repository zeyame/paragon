package com.paragon.integration.api;

import com.paragon.api.dtos.ErrorDto;
import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.getall.StaffAccountSummaryResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.api.exceptions.PermissionDeniedException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.TestJwtHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.infrastructure.persistence.jdbc.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

            RegisterStaffAccountResponseDto resultBody = responseDto.result();

            assertThat(resultBody.username()).isEqualTo(requestDto.username());
            assertThat(resultBody.status()).isEqualTo(StaffAccountStatus.PENDING_PASSWORD_CHANGE.toString());
            assertThat(resultBody.version()).isEqualTo(1);
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
                    "Password123!",
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
