package com.paragon.infrastructure.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestMetadataFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String correlationId = UUID.randomUUID().toString();

        request.setAttribute(RequestMetadataAttributes.IP_ADDRESS, ip);
        request.setAttribute(RequestMetadataAttributes.CORRELATION_ID, correlationId);

        MDC.put(RequestMetadataAttributes.IP_ADDRESS, ip);
        MDC.put(RequestMetadataAttributes.CORRELATION_ID, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(RequestMetadataAttributes.IP_ADDRESS);
            MDC.remove(RequestMetadataAttributes.CORRELATION_ID);
        }
    }
}

