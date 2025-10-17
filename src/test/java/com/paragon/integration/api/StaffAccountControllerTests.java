package com.paragon.integration.api;

import com.paragon.api.dtos.ErrorDto;
import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.getall.StaffAccountSummaryResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.aggregates.StaffAccount;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class StaffAccountControllerTests {
    @Nested
    class Register extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public Register(WriteJdbcHelper writeJdbcHelper) {
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldRegisterStaffAccount() throws Exception {
            // Given
            RegisterStaffAccountRequestDto requestDto = createValidRequest();

            // When
            MvcResult result = sendRequest(requestDto, adminId);

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            String responseBody = result.getResponse().getContentAsString();
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

            StaffAccount staffAccountLackingPermissions = new StaffAccountFixture().withCreatedBy(adminId).withPermissionCodes(List.of("VIEW_MODMAIL_LOGS")).build();
            jdbcHelper.insertStaffAccount(staffAccountLackingPermissions);

            // When
            MvcResult result = sendRequest(requestDto, staffAccountLackingPermissions.getId().getValue().toString());

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

            String responseBody = result.getResponse().getContentAsString();
            ResponseDto<RegisterStaffAccountResponseDto> responseDto = objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, RegisterStaffAccountResponseDto.class));

            RegisterStaffAccountResponseDto resultBody = responseDto.result();
            assertThat(resultBody).isNull();

            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();
            assertThat(errorDto.message()).isEqualTo(AppExceptionInfo.permissionAccessDenied("registration").getMessage());
            assertThat(errorDto.code()).isEqualTo(AppExceptionInfo.permissionAccessDenied("registration").getAppErrorCode());
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
            MvcResult result = sendRequest(requestDto, adminId);

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

        @Test
        void shouldReturnNotFoundWhenRequestingStaffAccountDoesNotExist() throws Exception {
            // Given
            RegisterStaffAccountRequestDto requestDto = createValidRequest();
            String nonExistentStaffId = UUID.randomUUID().toString();

            // When
            MvcResult result = sendRequest(requestDto, nonExistentStaffId);

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

            String responseBody = response.getContentAsString();
            ResponseDto<RegisterStaffAccountResponseDto> responseDto =
                    objectMapper.readValue(responseBody,
                            objectMapper.getTypeFactory()
                                    .constructParametricType(ResponseDto.class, RegisterStaffAccountResponseDto.class));

            assertThat(responseDto.result()).isNull();

            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();
            assertThat(errorDto.message()).isEqualTo(AppExceptionInfo.staffAccountNotFound(nonExistentStaffId).getMessage());
            assertThat(errorDto.code()).isEqualTo(AppExceptionInfo.staffAccountNotFound(nonExistentStaffId).getAppErrorCode());
        }

        private RegisterStaffAccountRequestDto createValidRequest() {
            return new RegisterStaffAccountRequestDto(
                    "john_doe",
                    "john_doe@example.com",
                    "Password123!",
                    10,
                    20,
                    List.of("VIEW_ACCOUNTS_LIST", "VIEW_LOGIN_LOGS")
            );
        }

        private MvcResult sendRequest(RegisterStaffAccountRequestDto requestDto, String actorId) throws Exception {
            MvcResult result = mockMvc.perform(
                    post("/v1/staff-accounts")
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
            ).andReturn();
            result = mockMvc.perform(asyncDispatch(result)).andReturn();
            return result;
        }
    }

    @Nested
    class GetAll extends IntegrationTestBase {
        private final TestJdbcHelper jdbcHelper;

        @Autowired
        public GetAll(WriteJdbcHelper writeJdbcHelper) {
            jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        }

        @Test
        void shouldReturnAllStaffAccounts() throws Exception {
            // Given
            StaffAccount staffAccount1 = new StaffAccountFixture()
                    .withUsername("john_doe")
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of("VIEW_ACCOUNTS_LIST"))
                    .build();
            StaffAccount staffAccount2 = new StaffAccountFixture()
                    .withUsername("jane_smith")
                    .withCreatedBy(adminId)
                    .withPermissionCodes(List.of("VIEW_LOGIN_LOGS"))
                    .build();
            jdbcHelper.insertStaffAccount(staffAccount1);
            jdbcHelper.insertStaffAccount(staffAccount2);

            // When
            MvcResult result = sendRequest(adminId);

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
                    .withPermissionCodes(List.of("VIEW_MODMAIL_LOGS"))
                    .build();
            jdbcHelper.insertStaffAccount(staffAccountLackingPermission);

            // When
            MvcResult result = sendRequest(staffAccountLackingPermission.getId().getValue().toString());

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());

            String responseBody = response.getContentAsString();
            ResponseDto<GetAllStaffAccountsResponseDto> responseDto = objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, GetAllStaffAccountsResponseDto.class));

            assertThat(responseDto.result()).isNull();

            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();
            assertThat(errorDto.message()).isEqualTo(AppExceptionInfo.permissionAccessDenied("view all registered staff accounts").getMessage());
            assertThat(errorDto.code()).isEqualTo(AppExceptionInfo.permissionAccessDenied("view all registered staff accounts").getAppErrorCode());
        }

        @Test
        void shouldReturnNotFoundWhenRequestingStaffAccountDoesNotExist() throws Exception {
            // Given
            String nonExistentStaffId = UUID.randomUUID().toString();

            // When
            MvcResult result = sendRequest(nonExistentStaffId);

            // Then
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

            String responseBody = response.getContentAsString();
            ResponseDto<GetAllStaffAccountsResponseDto> responseDto = objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, GetAllStaffAccountsResponseDto.class));

            assertThat(responseDto.result()).isNull();

            ErrorDto errorDto = responseDto.errorDto();
            assertThat(errorDto).isNotNull();
            assertThat(errorDto.message()).isEqualTo(AppExceptionInfo.staffAccountNotFound(nonExistentStaffId).getMessage());
            assertThat(errorDto.code()).isEqualTo(AppExceptionInfo.staffAccountNotFound(nonExistentStaffId).getAppErrorCode());
        }

        private MvcResult sendRequest(String actorId) throws Exception {
            MvcResult result = mockMvc.perform(
                    get("/v1/staff-accounts")
                            .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId))
            ).andReturn();
            result = mockMvc.perform(asyncDispatch(result)).andReturn();
            return result;
        }
    }
}
