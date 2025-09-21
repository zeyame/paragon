package com.paragon.api.dtos;

import lombok.Getter;

@Getter
public class ErrorDto {
    private final String message;
    private final int code;

    public ErrorDto(String message, int code) {
        this.message = message;
        this.code = code;
    }
}
