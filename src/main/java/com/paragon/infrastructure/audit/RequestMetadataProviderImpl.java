package com.paragon.infrastructure.audit;

import com.paragon.application.context.RequestMetadataProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class RequestMetadataProviderImpl implements RequestMetadataProvider {
    private final HttpServletRequest request;

    public RequestMetadataProviderImpl(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getIpAddress() {
        Object ip = request.getAttribute(RequestMetadataAttributes.IP_ADDRESS);
        return ip != null ? ip.toString() : "UNKNOWN";
    }

    @Override
    public String getCorrelationId() {
        Object corrId = request.getAttribute(RequestMetadataAttributes.CORRELATION_ID);
        return corrId != null ? corrId.toString() : "UNKNOWN";
    }
}