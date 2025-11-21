package com.paragon.application.common.interfaces;

import com.paragon.domain.models.valueobjects.PlaintextRefreshToken;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;

public interface TokenHasher {
    RefreshTokenHash hash(PlaintextRefreshToken plainToken);
}