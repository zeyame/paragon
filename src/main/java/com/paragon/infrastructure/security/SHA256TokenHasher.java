package com.paragon.infrastructure.security;

import com.paragon.application.common.interfaces.TokenHasher;
import com.paragon.domain.models.valueobjects.PlaintextRefreshToken;
import com.paragon.domain.models.valueobjects.RefreshTokenHash;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class SHA256TokenHasher implements TokenHasher {
    @Override
    public RefreshTokenHash hash(PlaintextRefreshToken plainToken) {
        String tokenHash = DigestUtils.sha256Hex(plainToken.getValue());
        return RefreshTokenHash.of(tokenHash);
    }
}