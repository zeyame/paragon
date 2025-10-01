package com.paragon.application.common.exceptions;

import com.paragon.domain.exceptions.DomainException;
import com.paragon.infrastructure.persistence.exceptions.InfraException;

public interface AppExceptionHandler {
    AppException handleDomainException(DomainException domainException);
    AppException handleInfraException(InfraException infraException);
}
