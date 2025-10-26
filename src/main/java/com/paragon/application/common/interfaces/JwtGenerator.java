package com.paragon.application.common.interfaces;

import com.paragon.domain.models.valueobjects.PermissionCode;
import com.paragon.domain.models.valueobjects.StaffAccountId;

import java.util.Set;

public interface JwtGenerator {
    String generateAccessToken(StaffAccountId staffAccountId, Set<PermissionCode> permissions);
}
