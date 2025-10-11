package com.paragon.helpers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public final class TestJwtHelper {
    private static final String SECRET = "a-string-secret-at-least-256-bits-long";

    private TestJwtHelper() {}

    public static String generateToken(String staffId) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(staffId)
                .claims(Map.of("staff_id", staffId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
