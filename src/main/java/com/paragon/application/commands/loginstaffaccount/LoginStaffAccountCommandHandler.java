package com.paragon.application.commands.loginstaffaccount;

import com.paragon.application.commands.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginStaffAccountCommandHandler implements CommandHandler<LoginStaffAccountCommand, LoginStaffAccountCommandResponse> {
    @Override
    public LoginStaffAccountCommandResponse handle(LoginStaffAccountCommand loginStaffAccountCommand) {
        return null;
    }
}
