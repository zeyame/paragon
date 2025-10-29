package com.paragon.application.commands.loginstaffaccount;

import java.util.List;

public record LoginStaffAccountCommandResponse(
        String id,
        String username,
        boolean requiresPasswordReset,
        String plainRefreshToken,
        List<String> permissionCodes,
        int version
) {}
