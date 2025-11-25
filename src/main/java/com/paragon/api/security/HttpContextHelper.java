package com.paragon.api.security;

public interface HttpContextHelper {
    String extractAuthenticatedStaffId();
    String extractIpAddress();
    String extractRefreshTokenFromCookie();
    void setJwtHeader(String jwt);
    void setRefreshTokenCookie(String plainRefreshToken);
    void clearRefreshTokenCookie();
}
