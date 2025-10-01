package com.paragon.application.common.exceptions;

import com.paragon.domain.exceptions.DomainException;
import com.paragon.infrastructure.persistence.exceptions.InfraException;
import org.springframework.stereotype.Component;

@Component
public class AppExceptionHandlerImpl implements AppExceptionHandler {
    @Override
    public AppException handleDomainException(DomainException domainException) {
        return null;
    }

    @Override
    public AppException handleInfraException(InfraException infraException) {
        return null;
    }
}
