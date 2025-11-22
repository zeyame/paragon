package com.paragon.api.controllers;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountRequestDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountResponseDto;
import com.paragon.api.dtos.auth.refresh.RefreshStaffAccountTokenResponseDto;
import com.paragon.api.security.HttpContextHelperImpl;
import com.paragon.api.security.JwtGenerator;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommand;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandHandler;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommand;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommandHandler;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommandResponse;
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
    private final HttpContextHelperImpl httpContextHelper;
    private final JwtGenerator jwtGenerator;
    private final TaskExecutor taskExecutor;
    private final RefreshStaffAccountTokenCommandHandler refreshStaffAccountTokenCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(LoginStaffAccountCommandHandler loginStaffAccountCommandHandler,
                          HttpContextHelperImpl httpContextHelper,
                          JwtGenerator jwtGenerator,
                          TaskExecutor taskExecutor,
                          RefreshStaffAccountTokenCommandHandler refreshStaffAccountTokenCommandHandler) {
        this.loginStaffAccountCommandHandler = loginStaffAccountCommandHandler;
        this.httpContextHelper = httpContextHelper;
        this.jwtGenerator = jwtGenerator;
        this.taskExecutor = taskExecutor;
        this.refreshStaffAccountTokenCommandHandler = refreshStaffAccountTokenCommandHandler;
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<ResponseDto<LoginStaffAccountResponseDto>>> login(@RequestBody LoginStaffAccountRequestDto requestDto) {
        log.info("Received request to login a staff account with username: {}", requestDto.username());

        return CompletableFuture.supplyAsync(() -> {
            String ipAddress = httpContextHelper.extractIpAddress();
            var command = createLoginStaffAccountCommand(requestDto, ipAddress);
            var commandResponse = loginStaffAccountCommandHandler.handle(command);

            httpContextHelper.setJwtHeader(jwtGenerator.generateAccessToken(commandResponse.id(), commandResponse.permissionCodes()));
            httpContextHelper.setRefreshTokenCookie(commandResponse.plainRefreshToken());

            var responseDto = new ResponseDto<>(LoginStaffAccountResponseDto.fromLoginStaffAccountCommandResponse(commandResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    @PostMapping("/refresh")
    public CompletableFuture<ResponseEntity<ResponseDto<RefreshStaffAccountTokenResponseDto>>> refresh() {
        log.info("Received request to refresh staff account token.");

        return CompletableFuture.supplyAsync(() -> {
            String plainRefreshToken = httpContextHelper.extractRefreshTokenFromCookie();
            var command = new RefreshStaffAccountTokenCommand(plainRefreshToken);
            RefreshStaffAccountTokenCommandResponse commandResponse = refreshStaffAccountTokenCommandHandler.handle(command);

            httpContextHelper.setJwtHeader(jwtGenerator.generateAccessToken(commandResponse.staffAccountId(), commandResponse.permissionCodes()));
            httpContextHelper.setRefreshTokenCookie(commandResponse.plainRefreshToken());

            var responseDto = new ResponseDto<>(createRefreshStaffAccountTokenResponseDto(commandResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    private LoginStaffAccountCommand createLoginStaffAccountCommand(LoginStaffAccountRequestDto requestDto, String ipAddress) {
        return new LoginStaffAccountCommand(
                requestDto.username(),
                requestDto.password(),
                ipAddress
        );
    }

    private RefreshStaffAccountTokenResponseDto createRefreshStaffAccountTokenResponseDto(RefreshStaffAccountTokenCommandResponse commandResponse) {
        return new RefreshStaffAccountTokenResponseDto(
                commandResponse.staffAccountId(),
                commandResponse.username(),
                commandResponse.requiresPasswordReset(),
                commandResponse.version()
        );
    }
}
