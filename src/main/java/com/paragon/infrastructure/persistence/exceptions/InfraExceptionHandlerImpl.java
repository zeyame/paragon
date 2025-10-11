package com.paragon.infrastructure.persistence.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InfraExceptionHandlerImpl implements InfraExceptionHandler {

    @Override
    public InfraException handleDatabaseException(DataAccessException e) {
        log.error("e: ", e);
        throw new InfraException(e.getMessage());
    }
}