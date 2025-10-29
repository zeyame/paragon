package com.paragon.application.commands.loginstaffaccount;

public record LoginStaffAccountCommand(
        String username,
        String password,
        String ipAddress
)
{}
