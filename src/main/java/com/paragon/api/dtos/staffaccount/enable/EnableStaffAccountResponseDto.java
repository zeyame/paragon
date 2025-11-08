package com.paragon.api.dtos.staffaccount.enable;

public record EnableStaffAccountResponseDto(
        String id,
        String status,
        String enabledBy,
        int version
) {
}
