package com.paragon.api.dtos.staffaccount.getall;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GetAllStaffAccountsResponseDto(
        @JsonProperty("staff_accounts")
        List<StaffAccountSummaryResponseDto> staffAccountSummaryResponseDtos
) {}
