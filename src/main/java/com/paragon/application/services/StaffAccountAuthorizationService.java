package com.paragon.application.services;

import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;

public interface StaffAccountAuthorizationService {
    void authorizeAction(StaffAccountId staffAccountId, PermissionCode requiredPermission);
}
