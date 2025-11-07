package com.paragon.application.commands.disablestaffaccount;

public record DisableStaffAccountCommandResponse(
        String id,
        String status,
        String disabledBy,
        int version
) {
}
