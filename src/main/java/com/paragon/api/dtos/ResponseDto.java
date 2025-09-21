package com.paragon.api.dtos;

import lombok.Getter;

@Getter
public class ResponseDto<T> {
    private final T result;
    private final ErrorDto errorDto;

    public ResponseDto(T result, ErrorDto errorDto) {
        this.result = result;
        this.errorDto = errorDto;
    }
}
