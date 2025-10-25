package com.paragon.application.common.interfaces;

public interface RequestMetadataProvider {
    String getIpAddress();
    String getCorrelationId();
}
