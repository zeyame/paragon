package com.paragon.api.mappers;

import com.paragon.api.dtos.staffaccountrequest.submitpasswordchangerequest.SubmitPasswordChangeRequestResponseDto;
import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommand;
import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommandResponse;

public class StaffAccountRequestMapper {
    public static SubmitPasswordChangeRequestCommand toSubmitPasswordChangeRequestCommand(String staffAccountId) {
        return new SubmitPasswordChangeRequestCommand(staffAccountId);
    }

    public static SubmitPasswordChangeRequestResponseDto toSubmitPasswordChangeRequestResponseDto(SubmitPasswordChangeRequestCommandResponse commandResponse) {
        return new SubmitPasswordChangeRequestResponseDto(
                commandResponse.requestId(),
                commandResponse.submittedBy(),
                commandResponse.requestType(),
                commandResponse.status(),
                commandResponse.submittedAtUtc(),
                commandResponse.expiresAtUtc()
        );
    }
}
