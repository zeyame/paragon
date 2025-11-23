package com.paragon.api.dtos.staffaccount.getall;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record StaffAccountSummaryResponseDto(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("username")
        String username,
        @JsonProperty("status")
        String status,
        @JsonProperty("created_at_utc")
        Instant createdAtUtc
) {}
