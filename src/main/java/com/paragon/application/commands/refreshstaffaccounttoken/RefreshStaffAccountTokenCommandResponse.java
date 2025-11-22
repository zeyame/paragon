package com.paragon.application.commands.refreshstaffaccounttoken;

import java.util.List;

public record RefreshStaffAccountTokenCommandResponse(
        String staffAccountId,
        String username,
        boolean requiresPasswordReset,
        String plainRefreshToken,
        List<String> permissionCodes,
        int version
) {
}
