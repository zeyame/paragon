package com.paragon.api.dtos.staffaccount.register;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterStaffAccountResponseDto(
        @JsonProperty("id")
        String id,

        @JsonProperty("username")
        String username,

        @JsonProperty("status")
        String status,

        @JsonProperty("version")
        int version
)
{}
