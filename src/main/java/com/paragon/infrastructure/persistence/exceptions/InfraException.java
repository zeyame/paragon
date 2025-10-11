package com.paragon.infrastructure.persistence.exceptions;

public class InfraException extends RuntimeException {
    public InfraException(String message) {
        super(message);
    }
}
