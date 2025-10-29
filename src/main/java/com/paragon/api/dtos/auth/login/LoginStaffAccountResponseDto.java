package com.paragon.api.dtos.auth.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandResponse;

public record LoginStaffAccountResponseDto(
        @JsonProperty("id")
        String id,
        @JsonProperty("username")
        String username,
        @JsonProperty("requires_password_reset")
        boolean requiresPasswordReset,
        @JsonProperty("version")
        int version
) {
        public static LoginStaffAccountResponseDto fromLoginStaffAccountCommandResponse(LoginStaffAccountCommandResponse commandResponse) {
                return new LoginStaffAccountResponseDto(
                        commandResponse.id(),
                        commandResponse.username(),
                        commandResponse.requiresPasswordReset(),
                        commandResponse.version()
                );
        }
}
