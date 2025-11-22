package com.paragon.api.dtos.auth.refresh;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommandResponse;

public record RefreshStaffAccountTokenResponseDto(
        @JsonProperty("id")
        String id,
        @JsonProperty("username")
        String username,
        @JsonProperty("requires_password_reset")
        boolean requiresPasswordReset,
        @JsonProperty("version")
        int version
) {}
