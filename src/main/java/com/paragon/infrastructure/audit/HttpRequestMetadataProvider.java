package com.paragon.infrastructure.audit;

import com.paragon.application.common.interfaces.RequestMetadataProvider;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestMetadataProvider implements RequestMetadataProvider {

    @Override
    public String getIpAddress() {
        return MDC.get(RequestMetadataAttributes.IP_ADDRESS);
    }

    @Override
    public String getCorrelationId() {
        return MDC.get(RequestMetadataAttributes.CORRELATION_ID);
    }
}
