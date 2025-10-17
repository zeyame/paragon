package com.paragon.api.dtos.auth.login;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginStaffAccountRequestDto(
        @JsonProperty("username")
        String username,
        @JsonProperty("password")
        String password
) {}
