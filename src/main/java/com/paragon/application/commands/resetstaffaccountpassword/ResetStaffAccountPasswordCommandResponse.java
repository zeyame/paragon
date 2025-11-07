package com.paragon.application.commands.resetstaffaccountpassword;

import java.time.Instant;

public record ResetStaffAccountPasswordCommandResponse(
        String id,
        String temporaryPassword,
        String status,
        Instant passwordIssuedAt,
        int version
) {
}
