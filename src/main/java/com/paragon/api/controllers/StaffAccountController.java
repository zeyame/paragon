package com.paragon.api.controllers;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.disable.DisableStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.api.dtos.staffaccount.resetpassword.ResetStaffAccountPasswordResponseDto;
import com.paragon.api.mappers.StaffAccountMapper;
import com.paragon.api.security.HttpContextHelper;
import com.paragon.application.commands.CommandHandler;
import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommand;
import com.paragon.application.commands.disablestaffaccount.DisableStaffAccountCommandResponse;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommand;
import com.paragon.application.commands.resetstaffaccountpassword.ResetStaffAccountPasswordCommandResponse;
import com.paragon.application.queries.QueryHandler;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQuery;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("v1/staff-accounts")
public class StaffAccountController {
    private final CommandHandler<RegisterStaffAccountCommand, RegisterStaffAccountCommandResponse> registerStaffAccountCommandHandler;
    private final CommandHandler<DisableStaffAccountCommand, DisableStaffAccountCommandResponse> disableStaffAccountCommandHandler;
    private final CommandHandler<ResetStaffAccountPasswordCommand, ResetStaffAccountPasswordCommandResponse> resetStaffAccountPasswordCommandHandler;
    private final QueryHandler<GetAllStaffAccountsQuery, GetAllStaffAccountsQueryResponse> getAllStaffAccountsQueryHandler;
    private final HttpContextHelper httpContextHelper;
    private final TaskExecutor taskExecutor;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountController.class);

    public StaffAccountController(
            CommandHandler<RegisterStaffAccountCommand, RegisterStaffAccountCommandResponse> registerStaffAccountCommandHandler,
            CommandHandler<DisableStaffAccountCommand, DisableStaffAccountCommandResponse> disableStaffAccountCommandHandler,
            CommandHandler<ResetStaffAccountPasswordCommand, ResetStaffAccountPasswordCommandResponse> resetStaffAccountPasswordCommandHandler,
            QueryHandler<GetAllStaffAccountsQuery, GetAllStaffAccountsQueryResponse> getAllStaffAccountsQueryHandler,
            HttpContextHelper httpContextHelper, TaskExecutor taskExecutor) {
        this.registerStaffAccountCommandHandler = registerStaffAccountCommandHandler;
        this.disableStaffAccountCommandHandler = disableStaffAccountCommandHandler;
        this.resetStaffAccountPasswordCommandHandler = resetStaffAccountPasswordCommandHandler;
        this.getAllStaffAccountsQueryHandler = getAllStaffAccountsQueryHandler;
        this.httpContextHelper = httpContextHelper;
        this.taskExecutor = taskExecutor;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ACCOUNTS')")
    public CompletableFuture<ResponseEntity<ResponseDto<RegisterStaffAccountResponseDto>>> register(@RequestBody RegisterStaffAccountRequestDto requestDto) {
        String requestingStaffAccountId = httpContextHelper.getAuthenticatedStaffId();
        log.info("Received request to register a new staff account from a staff account with ID: {}.", requestingStaffAccountId);

        return CompletableFuture.supplyAsync(() -> {
            var command = StaffAccountMapper.toRegisterCommand(requestDto, requestingStaffAccountId);
            var commandResponse = registerStaffAccountCommandHandler.handle(command);
            var responseDto = new ResponseDto<>(StaffAccountMapper.toRegisterResponseDto(commandResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    @PutMapping("/disable/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ACCOUNTS')")
    public CompletableFuture<ResponseEntity<ResponseDto<DisableStaffAccountResponseDto>>> disable(@PathVariable("id") String staffAccountIdToBeDisabled) {
        String requestingStaffAccountId = httpContextHelper.getAuthenticatedStaffId();
        log.info("Received request to disable a staff account with ID: {} was received from a staff account with ID: {}.",
                staffAccountIdToBeDisabled, requestingStaffAccountId);

        return CompletableFuture.supplyAsync(() -> {
            var command = StaffAccountMapper.toDisableCommand(staffAccountIdToBeDisabled, requestingStaffAccountId);
            var commandResponse = disableStaffAccountCommandHandler.handle(command);
            var responseDto = new ResponseDto<>(StaffAccountMapper.toDisableResponseDto(commandResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    @PutMapping("/reset-password/{id}")
    @PreAuthorize("hasAuthority('RESET_ACCOUNT_PASSWORD')")
    public CompletableFuture<ResponseEntity<ResponseDto<ResetStaffAccountPasswordResponseDto>>> resetPassword(@PathVariable("id") String staffAccountIdToReset) {
        String requestingStaffAccountId = httpContextHelper.getAuthenticatedStaffId();
        log.info("Received request to reset password for staff account ID: {} from staff account ID: {}.",
                staffAccountIdToReset, requestingStaffAccountId);

        return CompletableFuture.supplyAsync(() -> {
            var command = StaffAccountMapper.toResetPasswordCommand(staffAccountIdToReset, requestingStaffAccountId);
            var commandResponse = resetStaffAccountPasswordCommandHandler.handle(command);
            var responseDto = new ResponseDto<>(StaffAccountMapper.toResetPasswordResponseDto(commandResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ACCOUNTS_LIST')")
    public CompletableFuture<ResponseEntity<ResponseDto<GetAllStaffAccountsResponseDto>>> getAll() {
        String requestingStaffAccountId = httpContextHelper.getAuthenticatedStaffId();
        log.info("Received request to get all staff accounts from a staff account with ID: {}.", requestingStaffAccountId);

        return CompletableFuture.supplyAsync(() -> {
            var queryResponse = getAllStaffAccountsQueryHandler.handle(new GetAllStaffAccountsQuery());
            var responseDto = new ResponseDto<>(StaffAccountMapper.toGetAllResponseDto(queryResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

}
