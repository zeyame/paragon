package com.paragon.application.context;

public interface RequestMetadataProvider {
    String getIpAddress();
    String getCorrelationId();
}
