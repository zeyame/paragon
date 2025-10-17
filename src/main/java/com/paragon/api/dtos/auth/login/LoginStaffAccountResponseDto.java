package com.paragon.api.dtos.auth.login;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginStaffAccountResponseDto(
        @JsonProperty("id")
        String id,
        @JsonProperty("username")
        String username,
        @JsonProperty("requires_password_reset")
        boolean requiresPasswordReset,
        @JsonProperty("version")
        int version
) {}
