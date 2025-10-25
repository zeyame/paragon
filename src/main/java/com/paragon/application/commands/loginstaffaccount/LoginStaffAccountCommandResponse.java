package com.paragon.application.commands.loginstaffaccount;

public record LoginStaffAccountCommandResponse(
        String id,
        String username,
        boolean requiresPasswordReset,
        String plainRefreshToken,
        String jwtToken,
        int version
) {}
