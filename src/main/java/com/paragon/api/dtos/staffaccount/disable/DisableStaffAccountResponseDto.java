package com.paragon.api.dtos.staffaccount.disable;

public record DisableStaffAccountResponseDto(
        String id,
        String status,
        String disabledBy,
        int version
)
{}
