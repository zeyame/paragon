package com.paragon.api.controllers;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccountrequest.submitpasswordchangerequest.SubmitPasswordChangeRequestResponseDto;
import com.paragon.api.mappers.StaffAccountRequestMapper;
import com.paragon.api.security.HttpContextHelper;
import com.paragon.api.security.HttpContextHelperImpl;
import com.paragon.application.commands.CommandHandler;
import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommand;
import com.paragon.application.commands.submitstaffaccountpasswordchangerequest.SubmitPasswordChangeRequestCommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("v1/staff-account-requests")
public class StaffAccountRequestController {
    private final CommandHandler<SubmitPasswordChangeRequestCommand, SubmitPasswordChangeRequestCommandResponse> submitPasswordChangeRequestCommandHandler;
    private final HttpContextHelper httpContextHelper;
    private final TaskExecutor taskExecutor;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountRequestController.class);

    public StaffAccountRequestController(
            CommandHandler<SubmitPasswordChangeRequestCommand, SubmitPasswordChangeRequestCommandResponse> submitPasswordChangeRequestCommandHandler,
            HttpContextHelper httpContextHelper,
            TaskExecutor taskExecutor) {
        this.submitPasswordChangeRequestCommandHandler = submitPasswordChangeRequestCommandHandler;
        this.httpContextHelper = httpContextHelper;
        this.taskExecutor = taskExecutor;
    }

    @PostMapping("/password-change")
    public CompletableFuture<ResponseEntity<ResponseDto<SubmitPasswordChangeRequestResponseDto>>> submitPasswordChangeRequest() {
        String requestingStaffAccountId = httpContextHelper.extractAuthenticatedStaffId();
        log.info("Received request to submit password change request from staff account with ID: {}.", requestingStaffAccountId);

        return CompletableFuture.supplyAsync(() -> {
            var command = StaffAccountRequestMapper.toSubmitPasswordChangeRequestCommand(requestingStaffAccountId);
            var commandResponse = submitPasswordChangeRequestCommandHandler.handle(command);
            var responseDto = new ResponseDto<>(StaffAccountRequestMapper.toSubmitPasswordChangeRequestResponseDto(commandResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }
}
