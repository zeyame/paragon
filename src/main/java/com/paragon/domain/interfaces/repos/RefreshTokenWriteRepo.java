package com.paragon.domain.interfaces.repos;

import com.paragon.domain.models.aggregates.RefreshToken;

public interface RefreshTokenWriteRepo {
    void create(RefreshToken refreshToken);
}
