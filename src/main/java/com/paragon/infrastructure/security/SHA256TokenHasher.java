package com.paragon.infrastructure.security;

import com.paragon.domain.interfaces.TokenHasher;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class SHA256TokenHasher implements TokenHasher {
    @Override
    public String hash(String plainToken) {
        return DigestUtils.sha256Hex(plainToken);
    }
}