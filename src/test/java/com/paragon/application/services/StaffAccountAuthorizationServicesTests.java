package com.paragon.application.services;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.constants.SystemPermissions;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StaffAccountAuthorizationServicesTests {
    private final StaffAccountAuthorizationService sut;
    private final StaffAccountReadRepo staffAccountReadRepoMock;

    public StaffAccountAuthorizationServicesTests() {
        staffAccountReadRepoMock = mock(StaffAccountReadRepo.class);
        sut = new StaffAccountAuthorizationServiceImpl(staffAccountReadRepoMock);
    }

    @Test
    void shouldNotThrow_whenStaffAccountIsAuthorizedForAction() {
        // Given
        StaffAccountId staffAccountId = StaffAccountId.generate();
        PermissionCode permissionCode = SystemPermissions.MANAGE_ACCOUNTS;

        when(staffAccountReadRepoMock.findStatusById(staffAccountId.getValue()))
                .thenReturn(Optional.of(StaffAccountStatus.ACTIVE));
        when(staffAccountReadRepoMock.hasPermission(staffAccountId.getValue(), permissionCode))
                .thenReturn(true);

        // When & Then
        assertThatNoException().isThrownBy(() -> sut.authorizeAction(staffAccountId, permissionCode));
    }

    @Test
    void shouldThrowAppException_whenStaffAccountDoesNotExist() {
        // Given
        StaffAccountId staffAccountId = StaffAccountId.generate();

        when(staffAccountReadRepoMock.findStatusById(staffAccountId.getValue()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.authorizeAction(staffAccountId, SystemPermissions.MANAGE_ACCOUNTS))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(
                        AppExceptionInfo.staffAccountNotFound(staffAccountId.getValue().toString()).getMessage(),
                        AppExceptionInfo.staffAccountNotFound(staffAccountId.getValue().toString()).getAppErrorCode(),
                        AppExceptionInfo.staffAccountNotFound(staffAccountId.getValue().toString()).getStatusCode()
                );

    }

    @ParameterizedTest
    @MethodSource("provideInvalidStatuses")
    void shouldThrowAppException_whenStaffAccountIsNotActive(StaffAccountStatus status) {
        // Given
        StaffAccountId staffAccountId = StaffAccountId.generate();

        when(staffAccountReadRepoMock.findStatusById(staffAccountId.getValue()))
                .thenReturn(Optional.of(status));

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.authorizeAction(staffAccountId, SystemPermissions.MANAGE_ACCOUNTS))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(
                        AppExceptionInfo.staffAccountNotActive(staffAccountId.getValue().toString()).getMessage(),
                        AppExceptionInfo.staffAccountNotActive(staffAccountId.getValue().toString()).getAppErrorCode(),
                        AppExceptionInfo.staffAccountNotActive(staffAccountId.getValue().toString()).getStatusCode()
                );
    }

    @Test
    void shouldThrowAppException_whenStaffAccountLacksTheRequiredPermission() {
        // Given
        StaffAccountId staffAccountId = StaffAccountId.generate();
        PermissionCode permissionCode = SystemPermissions.MANAGE_ACCOUNTS;

        when(staffAccountReadRepoMock.findStatusById(staffAccountId.getValue()))
                .thenReturn(Optional.of(StaffAccountStatus.ACTIVE));
        when(staffAccountReadRepoMock.hasPermission(staffAccountId.getValue(), permissionCode))
                .thenReturn(false);

        // When & Then
        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> sut.authorizeAction(staffAccountId, SystemPermissions.MANAGE_ACCOUNTS))
                .extracting("message", "errorCode", "statusCode")
                .containsExactly(
                        AppExceptionInfo.missingRequiredPermission(staffAccountId.getValue().toString(), permissionCode.getValue()).getMessage(),
                        AppExceptionInfo.missingRequiredPermission(staffAccountId.getValue().toString(), permissionCode.getValue()).getAppErrorCode(),
                        AppExceptionInfo.missingRequiredPermission(staffAccountId.getValue().toString(), permissionCode.getValue()).getStatusCode()
                );
    }

    private static Stream<Arguments> provideInvalidStatuses() {
        return Stream.of(
                arguments(StaffAccountStatus.PENDING_PASSWORD_CHANGE),
                arguments(StaffAccountStatus.DISABLED),
                arguments(StaffAccountStatus.LOCKED)
        );
    }

}
