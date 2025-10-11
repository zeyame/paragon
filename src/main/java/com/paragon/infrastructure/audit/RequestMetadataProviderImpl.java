package com.paragon.infrastructure.audit;

import com.paragon.application.context.RequestMetadataProvider;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class RequestMetadataProviderImpl implements RequestMetadataProvider {

    @Override
    public String getIpAddress() {
        return MDC.get(RequestMetadataAttributes.IP_ADDRESS);
    }

    @Override
    public String getCorrelationId() {
        return MDC.get(RequestMetadataAttributes.CORRELATION_ID);
    }
}
