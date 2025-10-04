package com.paragon.infrastructure.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestMetadataFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = UUID.randomUUID().toString();
        String ipAddress = request.getRemoteAddr();

        request.setAttribute(RequestMetadataAttributes.CORRELATION_ID, correlationId);
        request.setAttribute(RequestMetadataAttributes.IP_ADDRESS, ipAddress);

        response.setHeader(RequestMetadataAttributes.CORRELATION_ID, correlationId);
        MDC.put("correlationId", correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
