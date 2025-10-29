package com.paragon.application.commands.loginstaffaccount;

import java.util.Set;

public record LoginStaffAccountCommandResponse(
        String id,
        String username,
        boolean requiresPasswordReset,
        String plainRefreshToken,
        Set<String> permissionCodes,
        int version
) {}
