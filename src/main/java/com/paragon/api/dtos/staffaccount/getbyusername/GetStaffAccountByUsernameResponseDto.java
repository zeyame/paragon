package com.paragon.api.dtos.staffaccount.getbyusername;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record GetStaffAccountByUsernameResponseDto(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("username")
        String username,
        @JsonProperty("status")
        String status,
        @JsonProperty("order_access_duration")
        Integer orderAccessDuration,
        @JsonProperty("modmail_transcript_access_duration")
        Integer modmailTranscriptAccessDuration,
        @JsonProperty("created_at")
        Instant createdAt
) {
    public static GetStaffAccountByUsernameResponseDto empty() {
        return new GetStaffAccountByUsernameResponseDto(
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
