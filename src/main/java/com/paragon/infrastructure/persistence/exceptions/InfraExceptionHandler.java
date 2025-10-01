package com.paragon.infrastructure.persistence.exceptions;

import org.springframework.dao.DataAccessException;

public interface InfraExceptionHandler {
    InfraException handleDatabaseException(DataAccessException e);
}
