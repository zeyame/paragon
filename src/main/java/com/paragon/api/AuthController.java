package com.paragon.api;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountRequestDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountResponseDto;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommand;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandHandler;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("v1/auth")
public class AuthController {
    private final LoginStaffAccountCommandHandler loginStaffAccountCommandHandler;
    private final TaskExecutor taskExecutor;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(LoginStaffAccountCommandHandler loginStaffAccountCommandHandler) {
        this.loginStaffAccountCommandHandler = loginStaffAccountCommandHandler;
        this.taskExecutor = Runnable::run;
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<ResponseDto<LoginStaffAccountResponseDto>>> login(@RequestBody LoginStaffAccountRequestDto requestDto) {
        log.info("Received request to login a staff account with username: {}", requestDto.username());

        return CompletableFuture.supplyAsync(() -> {
            var command = createLoginStaffAccountCommand(requestDto);
            var commandResponse = loginStaffAccountCommandHandler.handle(command);
            var responseDto = new ResponseDto<>(createLoginStaffAccountResponseDto(commandResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    private LoginStaffAccountCommand createLoginStaffAccountCommand(LoginStaffAccountRequestDto requestDto) {
        return new LoginStaffAccountCommand(
                requestDto.username(),
                requestDto.password()
        );
    }

    private LoginStaffAccountResponseDto createLoginStaffAccountResponseDto(LoginStaffAccountCommandResponse commandResponse) {
        return new LoginStaffAccountResponseDto(
                commandResponse.id(),
                commandResponse.username(),
                commandResponse.requiresPasswordReset(),
                commandResponse.version()
        );
    }
}
