package com.paragon.api.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class HttpContextHelper {

    public String getAuthenticatedStaffId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("staff_id");
        }

        throw new IllegalStateException("Unable to extract staff_id from authentication");
    }

    public String extractIpAddress() {
        HttpServletRequest request = getCurrentRequest();

        // Check for X-Forwarded-For header (proxy/load balancer scenarios)
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // X-Forwarded-For can contain multiple IPs (client, proxy1, proxy2...)
            // The first IP is the original client
            return forwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header (alternative proxy header)
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }

    public void setJwtHeader(String jwt) {
        HttpServletResponse response = getCurrentResponse();
        response.setHeader("Authorization", "Bearer " + jwt);
    }

    public void setRefreshTokenCookie(String plainRefreshToken) {
        HttpServletResponse response = getCurrentResponse();

        Cookie refreshTokenCookie = new Cookie("refresh_token", plainRefreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Only sent over HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days in seconds

        response.addCookie(refreshTokenCookie);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No request context available");
        }
        return attributes.getRequest();
    }

    private HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No request context available");
        }
        return attributes.getResponse();
    }
}