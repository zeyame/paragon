package com.paragon.api.errorhandling;

import com.paragon.api.dtos.ErrorDto;
import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.exceptions.PermissionDeniedException;
import com.paragon.application.common.exceptions.AppException;
import com.paragon.application.common.exceptions.AppExceptionStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionFilter {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ResponseDto<Void>> handleAppException(AppException exception) {
        var errorDto = new ErrorDto(
                exception.getMessage(),
                exception.getErrorCode()
        );
        ResponseDto<Void> responseDto = new ResponseDto<>(null, errorDto);
        return ResponseEntity
                .status(mapToHttpStatusCode(exception.getStatusCode()))
                .body(responseDto);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ResponseDto<Void>> handlePermissionDeniedException(PermissionDeniedException exception) {
        var errorDto = new ErrorDto(
                exception.getMessage(),
                exception.getErrorCode()
        );
        ResponseDto<Void> responseDto = new ResponseDto<>(null, errorDto);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(responseDto);
    }

    private HttpStatus mapToHttpStatusCode(AppExceptionStatusCode appExceptionStatusCode) {
        return switch (appExceptionStatusCode) {
            case CLIENT_ERROR -> HttpStatus.BAD_REQUEST;
            case SERVER_ERROR, UNHANDLED_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RESOURCE_OWNERSHIP_VIOLATION, PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case INVALID_RESOURCE_STATE, RESOURCE_UNIQUENESS_VIOLATION -> HttpStatus.CONFLICT;
            case AUTHENTICATION_FAILED -> HttpStatus.UNAUTHORIZED;
        };
    }
}
