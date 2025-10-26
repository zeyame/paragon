package com.paragon.api.exceptions;

import lombok.Getter;

@Getter
public class PermissionDeniedException extends RuntimeException {
    private final int errorCode;

    public PermissionDeniedException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public static PermissionDeniedException accessDenied() {
        return new PermissionDeniedException(
                "You do not have access to perform this action",
                999
        );
    }
}