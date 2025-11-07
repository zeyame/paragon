package com.paragon.application.commands.registerstaffaccount;

import java.util.List;

public record RegisterStaffAccountCommand(
        String username,
        String email,
        int orderAccessDuration,
        int modmailTranscriptAccessDuration,
        List<String> permissionCodes,
        String createdBy
) {}
