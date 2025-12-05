package com.paragon.api.dtos.auth.completetemporarypassword;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommandResponse;

public record CompleteTemporaryPasswordResponseDto(
        @JsonProperty("id")
        String id,
        @JsonProperty("username")
        String username,
        @JsonProperty("status")
        String status,
        @JsonProperty("version")
        int version
) {
    public static CompleteTemporaryPasswordResponseDto fromCommandResponse(CompleteTemporaryStaffAccountPasswordChangeCommandResponse commandResponse) {
        return new CompleteTemporaryPasswordResponseDto(
                commandResponse.id(),
                commandResponse.username(),
                commandResponse.status(),
                commandResponse.version()
        );
    }
}