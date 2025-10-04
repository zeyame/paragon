package com.paragon.infrastructure.security;

import com.paragon.application.context.ActorContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class ActorContextImpl implements ActorContext {
    @Override
    public String getActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("staff_id");
        }

        return "Unknown";
    }
}
