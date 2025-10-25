package com.paragon.infrastructure.security;

import com.paragon.application.common.interfaces.JwtGenerator;
import com.paragon.domain.models.valueobjects.StaffAccountId;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtGeneratorImpl implements JwtGenerator {
    private static final long ACCESS_TOKEN_EXPIRY_MINUTES = 15;
    private final JwtEncoder jwtEncoder;

    public JwtGeneratorImpl(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Override
    public String generateAccessToken(StaffAccountId staffAccountId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ACCESS_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("staff_id", staffAccountId.getValue().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}