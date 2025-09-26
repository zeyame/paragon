package com.paragon.application.common.exceptions;

import com.paragon.domain.exceptions.DomainException;

public interface AppExceptionHandler {
    AppException handleDomainException(DomainException domainException);
}
