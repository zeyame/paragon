package com.paragon.application.common.exceptions;

import com.paragon.domain.exceptions.DomainException;
import org.springframework.stereotype.Component;

@Component
public class AppExceptionHandlerImpl implements AppExceptionHandler {
    @Override
    public AppException handleDomainException(DomainException domainException) {
        return null;
    }
}
