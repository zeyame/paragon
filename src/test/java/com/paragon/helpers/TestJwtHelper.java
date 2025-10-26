package com.paragon.helpers;

import com.paragon.domain.models.valueobjects.PermissionCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public final class TestJwtHelper {
    private static final String SECRET = "a-string-secret-at-least-256-bits-long";

    private TestJwtHelper() {}

    public static String generateToken(String staffId, List<PermissionCode> permissions) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> claims = new HashMap<>();
        claims.put("staff_id", staffId);
        claims.put("permissions", permissions.stream().map(PermissionCode::getValue).toList());

        return Jwts.builder()
                .subject(staffId)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
