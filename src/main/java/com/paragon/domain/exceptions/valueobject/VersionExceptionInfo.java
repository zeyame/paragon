package com.paragon.domain.exceptions.valueobject;

import com.paragon.domain.exceptions.DomainExceptionInfo;

public class VersionExceptionInfo extends DomainExceptionInfo {
    private VersionExceptionInfo(String message, int domainErrorCode) {
        super(message, domainErrorCode);
    }
    
    public static VersionExceptionInfo mustBeAtleastOne() {
        return new VersionExceptionInfo(
                "Version number cannot be below 1.",
                100001
        );
    }
}
