package com.paragon.api;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.staffaccount.getall.GetAllStaffAccountsResponseDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountRequestDto;
import com.paragon.api.dtos.staffaccount.register.RegisterStaffAccountResponseDto;
import com.paragon.application.commands.CommandHandler;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommand;
import com.paragon.application.commands.registerstaffaccount.RegisterStaffAccountCommandResponse;
import com.paragon.application.queries.QueryHandler;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQuery;
import com.paragon.application.queries.getallstaffaccounts.GetAllStaffAccountsQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping(("v1/staff-accounts"))
public class StaffAccountController {
    private final CommandHandler<RegisterStaffAccountCommand, RegisterStaffAccountCommandResponse> registerStaffAccountCommandHandler;
    private final QueryHandler<GetAllStaffAccountsQuery, GetAllStaffAccountsQueryResponse> getAllStaffAccountsQueryHandler;
    private final TaskExecutor taskExecutor;

    public StaffAccountController(CommandHandler<RegisterStaffAccountCommand, RegisterStaffAccountCommandResponse> registerStaffAccountCommandHandler, QueryHandler<GetAllStaffAccountsQuery, GetAllStaffAccountsQueryResponse> getAllStaffAccountsQueryHandler) {
        this.registerStaffAccountCommandHandler = registerStaffAccountCommandHandler;
        this.getAllStaffAccountsQueryHandler = getAllStaffAccountsQueryHandler;
        this.taskExecutor = Runnable::run;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<ResponseDto<RegisterStaffAccountResponseDto>>> register(@RequestBody RegisterStaffAccountRequestDto requestDto) {
        log.info("Received request to register a new staff account.");

        return CompletableFuture.supplyAsync(() -> {
            var command = createRegisterStaffAccountCommand(requestDto);
            var commandResponse = registerStaffAccountCommandHandler.handle(command);
            var responseDto = new ResponseDto<>(createRegisterStaffAccountResponseDto(commandResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<ResponseDto<GetAllStaffAccountsResponseDto>>> getAll() {
        log.info("Received request to get all staff accounts.");

        return CompletableFuture.supplyAsync(() -> {
            var queryResponse = getAllStaffAccountsQueryHandler.handle(new GetAllStaffAccountsQuery());
            var responseDto = new ResponseDto<>(createGetAllStaffAccountsResponseDto(queryResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    private RegisterStaffAccountCommand createRegisterStaffAccountCommand(RegisterStaffAccountRequestDto requestDto) {
        return new RegisterStaffAccountCommand(
                requestDto.username(),
                requestDto.email(),
                requestDto.tempPassword(),
                requestDto.orderAccessDuration(),
                requestDto.modmailTranscriptAccessDuration(),
                requestDto.permissionCodes()
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
        return new GetAllStaffAccountsResponseDto();
    }

}
