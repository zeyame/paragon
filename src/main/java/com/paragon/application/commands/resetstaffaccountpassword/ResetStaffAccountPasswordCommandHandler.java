package com.paragon.application.commands.resetstaffaccountpassword;

import com.paragon.application.commands.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ResetStaffAccountPasswordCommandHandler implements CommandHandler<ResetStaffAccountPasswordCommand, ResetStaffAccountPasswordCommandResponse> {
    @Override
    public ResetStaffAccountPasswordCommandResponse handle(ResetStaffAccountPasswordCommand command) {
        return null;
    }
}
