package com.paragon.application.services;

import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionInfo;
import com.paragon.application.queries.repositoryinterfaces.StaffAccountReadRepo;
import com.paragon.domain.enums.StaffAccountStatus;
import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;

public class StaffAccountAuthorizationServiceImpl implements StaffAccountAuthorizationService {
    private final StaffAccountReadRepo staffAccountReadRepo;

    public StaffAccountAuthorizationServiceImpl(StaffAccountReadRepo staffAccountReadRepo) {
        this.staffAccountReadRepo = staffAccountReadRepo;
    }

    @Override
    public void authorizeAction(StaffAccountId staffAccountId, PermissionCode requiredPermission) {
        StaffAccountStatus status = staffAccountReadRepo.findStatusById(staffAccountId.getValue())
                .orElseThrow(() -> new AppException(AppExceptionInfo.staffAccountNotFound(staffAccountId.getValue().toString())));

        if (status != StaffAccountStatus.ACTIVE) {
            throw new AppException(AppExceptionInfo.staffAccountNotActive(staffAccountId.getValue().toString()));
        }

        if (!staffAccountReadRepo.hasPermission(staffAccountId.getValue(), requiredPermission)) {
            throw new AppException(AppExceptionInfo.missingRequiredPermission(staffAccountId.getValue().toString(), requiredPermission.getValue()));
        }
    }
}
