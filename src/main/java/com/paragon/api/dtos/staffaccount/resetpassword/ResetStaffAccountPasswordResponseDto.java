package com.paragon.api.dtos.staffaccount.resetpassword;

import java.time.Instant;

public record ResetStaffAccountPasswordResponseDto(
        String id,
        String temporaryPassword,
        String status,
        Instant passwordIssuedAt,
        int version
) {
}
