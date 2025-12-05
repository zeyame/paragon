package com.paragon.api.dtos.auth.completetemporarypassword;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompleteTemporaryPasswordRequestDto(
        @JsonProperty("id")
        String id,
        @JsonProperty("new_password")
        String newPassword
) {}