package com.paragon.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResponseDto<T>(
        @JsonProperty("result")
        T result,

        @JsonProperty("error")
        ErrorDto errorDto
)
{}
