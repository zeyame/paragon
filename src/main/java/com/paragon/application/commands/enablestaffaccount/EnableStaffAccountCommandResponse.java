package com.paragon.application.commands.enablestaffaccount;

public record EnableStaffAccountCommandResponse(
        String id,
        String status,
        String enabledBy,
        int version
) {
}
