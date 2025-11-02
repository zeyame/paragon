package com.paragon.api;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.getall.StaffAccountSummaryResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.api.security.HttpContextHelper;
import com.paragon.application.commands.CommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.queries.QueryHandler;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQuery;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryResponse;
import com.paragon.application.queries.getallstaffaccounts.StaffAccountSummary;
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
    private final QueryHandler<GetAllStaffAccountsQuery, GetAllStaffAccountsQueryResponse> getAllStaffAccountsQueryHandler;
    private final HttpContextHelper httpContextHelper;
    private final TaskExecutor taskExecutor;
    private static final Logger log = LoggerFactory.getLogger(StaffAccountController.class);

    public StaffAccountController(
            CommandHandler<RegisterStaffAccountCommand, RegisterStaffAccountCommandResponse> registerStaffAccountCommandHandler,
            QueryHandler<GetAllStaffAccountsQuery, GetAllStaffAccountsQueryResponse> getAllStaffAccountsQueryHandler,
            HttpContextHelper httpContextHelper, TaskExecutor taskExecutor) {
        this.registerStaffAccountCommandHandler = registerStaffAccountCommandHandler;
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
            var command = createRegisterStaffAccountCommand(requestDto, requestingStaffAccountId);
            var commandResponse = registerStaffAccountCommandHandler.handle(command);
            var responseDto = new ResponseDto<>(createRegisterStaffAccountResponseDto(commandResponse), null);
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
            var responseDto = new ResponseDto<>(createGetAllStaffAccountsResponseDto(queryResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    private RegisterStaffAccountCommand createRegisterStaffAccountCommand(RegisterStaffAccountRequestDto requestDto, String requestingStaffAccountId) {
        return new RegisterStaffAccountCommand(
                requestDto.username(),
                requestDto.email(),
                requestDto.tempPassword(),
                requestDto.orderAccessDuration(),
                requestDto.modmailTranscriptAccessDuration(),
                requestDto.permissionCodes(),
                requestingStaffAccountId
        );
    }

    private RegisterStaffAccountResponseDto createRegisterStaffAccountResponseDto(RegisterStaffAccountCommandResponse commandResponse) {
        return new RegisterStaffAccountResponseDto(
                commandResponse.id(),
                commandResponse.username(),
                commandResponse.status(),
                commandResponse.version()
        );
    }

    private GetAllStaffAccountsResponseDto createGetAllStaffAccountsResponseDto(GetAllStaffAccountsQueryResponse queryResponse) {
        return new GetAllStaffAccountsResponseDto(
                queryResponse.staffAccountSummaries()
                        .stream()
                        .map(this::toStaffAccountSummaryResponseDto)
                        .toList()
        );
    }

    private StaffAccountSummaryResponseDto toStaffAccountSummaryResponseDto(StaffAccountSummary staffAccountSummary) {
        return new StaffAccountSummaryResponseDto(
                staffAccountSummary.id(),
                staffAccountSummary.username(),
                staffAccountSummary.status(),
                staffAccountSummary.orderAccessDuration(),
                staffAccountSummary.modmailTranscriptAccessDuration(),
                staffAccountSummary.createdAtUtc()
        );
    }

}
