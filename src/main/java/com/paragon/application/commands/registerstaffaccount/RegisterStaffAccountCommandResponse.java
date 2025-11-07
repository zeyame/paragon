package com.paragon.application.commands.registerstaffaccount;

public record RegisterStaffAccountCommandResponse(
        String id,
        String username,
        String tempPassword,
        String status,
        int version
) {}
