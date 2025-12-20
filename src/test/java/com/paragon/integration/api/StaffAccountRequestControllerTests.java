package com.paragon.integration.api;

import com.paragon.api.dtos.ErrorDto;
import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccountrequest.submitpasswordchangerequest.SubmitPasswordChangeRequestResponseDto;
import com.paragon.api.exceptions.PermissionDeniedException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.domain.enums.StaffAccountRequestStatus;
import com.paragon.domain.enums.StaffAccountRequestType;
import com.paragon.domain.models.aggregates.StaffAccount;
import com.paragon.domain.models.aggregates.StaffAccountRequest;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import com.paragon.domain.models.valueobjects.StaffAccountRequestId;
import com.paragon.helpers.TestJdbcHelper;
import com.paragon.helpers.TestJwtHelper;
import com.paragon.helpers.fixtures.StaffAccountFixture;
import com.paragon.helpers.fixtures.StaffAccountRequestFixture;
import com.paragon.infrastructure.persistence.jdbc.helpers.WriteJdbcHelper;
import com.paragon.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class StaffAccountRequestControllerTests extends IntegrationTestBase {
    private final TestJdbcHelper jdbcHelper;
    private final List<PermissionCode> adminPermissions;

    @Autowired
    public StaffAccountRequestControllerTests(WriteJdbcHelper writeJdbcHelper) {
        jdbcHelper = new TestJdbcHelper(writeJdbcHelper);
        adminPermissions = jdbcHelper.getPermissionsForStaff(StaffAccountId.from(adminId));
    }

    @Test
    void shouldSubmitPasswordChangeRequest() throws Exception {
        // When
        MvcResult result = sendAsyncRequest(adminId, adminPermissions);

        // Then
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        String responseBody = response.getContentAsString();
        ResponseDto<SubmitPasswordChangeRequestResponseDto> responseDto = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, SubmitPasswordChangeRequestResponseDto.class)
        );

        assertThat(responseDto.result()).isNotNull();
        assertThat(responseDto.errorDto()).isNull();

        SubmitPasswordChangeRequestResponseDto resultBody = responseDto.result();
        assertThat(resultBody.requestId()).isNotNull();
        assertThat(resultBody.submittedBy()).isEqualTo(adminId);
        assertThat(resultBody.requestType()).isEqualTo(StaffAccountRequestType.PASSWORD_CHANGE.toString());
        assertThat(resultBody.status()).isEqualTo(StaffAccountRequestStatus.PENDING.toString());
        assertThat(resultBody.submittedAtUtc()).isNotNull();
        assertThat(resultBody.expiresAtUtc()).isNotNull();

        // verify that request was persisted
        Optional<StaffAccountRequest> persistedRequest = jdbcHelper.getStaffAccountRequestById(
                StaffAccountRequestId.of(UUID.fromString(resultBody.requestId()))
        );
        assertThat(persistedRequest).isPresent();
        assertThat(persistedRequest.get().getSubmittedBy()).isEqualTo(StaffAccountId.from(adminId));
        assertThat(persistedRequest.get().getRequestType()).isEqualTo(StaffAccountRequestType.PASSWORD_CHANGE);
        assertThat(persistedRequest.get().getStatus()).isEqualTo(StaffAccountRequestStatus.PENDING);
    }

    @Test
    void shouldReturnConflictWhenPendingPasswordChangeRequestAlreadyExists() throws Exception {
        // Given
        StaffAccount staffAccount = new StaffAccountFixture()
                .withCreatedBy(adminId)
                .build();
        jdbcHelper.insertStaffAccount(staffAccount);

        StaffAccountRequest existingRequest = new StaffAccountRequestFixture()
                .withSubmittedBy(staffAccount.getId().getValue().toString())
                .withRequestType(StaffAccountRequestType.PASSWORD_CHANGE)
                .withStatus(StaffAccountRequestStatus.PENDING)
                .build();
        jdbcHelper.insertStaffAccountRequest(existingRequest);

        // When
        MvcResult result = sendAsyncRequest(
                staffAccount.getId().getValue().toString(),
                List.of(SystemPermissions.VIEW_ACCOUNTS_LIST)
        );

        // Then
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

        String responseBody = response.getContentAsString();
        ResponseDto<SubmitPasswordChangeRequestResponseDto> responseDto = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, SubmitPasswordChangeRequestResponseDto.class)
        );

        assertThat(responseDto.result()).isNull();
        ErrorDto errorDto = responseDto.errorDto();
        assertThat(errorDto).isNotNull();

        AppExceptionInfo expectedInfo = AppExceptionInfo.pendingStaffAccountRequestAlreadyExists(
                staffAccount.getUsername().getValue(),
                StaffAccountRequestType.PASSWORD_CHANGE.toString()
        );
        assertThat(errorDto.message()).isEqualTo(expectedInfo.getMessage());
        assertThat(errorDto.code()).isEqualTo(expectedInfo.getAppErrorCode());
    }

    @Test
    void shouldReturnNotFoundWhenStaffAccountDoesNotExist() throws Exception {
        // Given
        String nonExistentStaffAccountId = UUID.randomUUID().toString();

        // When
        MvcResult result = sendAsyncRequest(
                nonExistentStaffAccountId,
                List.of(SystemPermissions.VIEW_ACCOUNTS_LIST)
        );

        // Then
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        String responseBody = response.getContentAsString();
        ResponseDto<SubmitPasswordChangeRequestResponseDto> responseDto = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructParametricType(ResponseDto.class, SubmitPasswordChangeRequestResponseDto.class)
        );

        assertThat(responseDto.result()).isNull();
        ErrorDto errorDto = responseDto.errorDto();
        assertThat(errorDto).isNotNull();

        AppExceptionInfo expectedInfo = AppExceptionInfo.staffAccountNotFound(nonExistentStaffAccountId);
        assertThat(errorDto.message()).isEqualTo(expectedInfo.getMessage());
        assertThat(errorDto.code()).isEqualTo(expectedInfo.getAppErrorCode());
    }

    private MvcResult sendRequest(String actorId, List<PermissionCode> permissions) throws Exception {
        return mockMvc.perform(
                post("/v1/staff-account-requests/password-change")
                        .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
        ).andReturn();
    }

    private MvcResult sendAsyncRequest(String actorId, List<PermissionCode> permissions) throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                post("/v1/staff-account-requests/password-change")
                        .header("Authorization", "Bearer " + TestJwtHelper.generateToken(actorId, permissions))
        ).andReturn();

        return mockMvc.perform(asyncDispatch(mvcResult)).andReturn();
    }
}
