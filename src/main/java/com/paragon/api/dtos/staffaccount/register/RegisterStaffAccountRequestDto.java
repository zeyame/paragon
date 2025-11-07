package com.paragon.api.dtos.staffaccount.register;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RegisterStaffAccountRequestDto (
    @JsonProperty("username")
    String username,

    @JsonProperty("email")
    String email,

    @JsonProperty("order_access_duration")
    int orderAccessDuration,

    @JsonProperty("modmail_transcript_access_duration")
    int modmailTranscriptAccessDuration,

    @JsonProperty("permission_codes")
    List<String> permissionCodes
) {}
