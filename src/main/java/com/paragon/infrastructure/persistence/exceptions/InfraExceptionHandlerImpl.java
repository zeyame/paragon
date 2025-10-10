package com.paragon.infrastructure.persistence.exceptions;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class InfraExceptionHandlerImpl implements InfraExceptionHandler {

    @Override
    public InfraException handleDatabaseException(DataAccessException e) {
        // Log the original exception and wrap it
        throw new InfraException();
    }
}