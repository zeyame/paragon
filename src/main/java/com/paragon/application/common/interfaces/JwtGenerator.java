package com.paragon.application.common.interfaces;

import com.paragon.domain.models.valueobjects.StaffAccountId;

public interface JwtGenerator {
    String generateAccessToken(StaffAccountId staffAccountId);
}
