package com.paragon.api.controllers;

import com.paragon.api.dtos.ResponseDto;
import com.paragon.api.dtos.auth.completetemporarypassword.CompleteTemporaryPasswordRequestDto;
import com.paragon.api.dtos.auth.completetemporarypassword.CompleteTemporaryPasswordResponseDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountRequestDto;
import com.paragon.api.dtos.auth.login.LoginStaffAccountResponseDto;
import com.paragon.api.dtos.auth.refresh.RefreshStaffAccountTokenResponseDto;
import com.paragon.api.security.HttpContextHelper;
import com.paragon.api.security.JwtGenerator;
import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommand;
import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommandHandler;
import com.paragon.application.commands.completetemporarystaffaccountpasswordchange.CompleteTemporaryStaffAccountPasswordChangeCommandResponse;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommand;
import com.paragon.application.commands.loginstaffaccount.LoginStaffAccountCommandHandler;
import com.paragon.application.commands.logoutstaffaccount.LogoutStaffAccountCommand;
import com.paragon.application.commands.logoutstaffaccount.LogoutStaffAccountCommandHandler;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommand;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommandHandler;
import com.paragon.application.commands.refreshstaffaccounttoken.RefreshStaffAccountTokenCommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("v1/auth")
public class AuthController {
    private final LoginStaffAccountCommandHandler loginStaffAccountCommandHandler;
    private final HttpContextHelper httpContextHelper;
    private final JwtGenerator jwtGenerator;
    private final TaskExecutor taskExecutor;
    private final RefreshStaffAccountTokenCommandHandler refreshStaffAccountTokenCommandHandler;
    private final LogoutStaffAccountCommandHandler logoutStaffAccountCommandHandler;
    private final CompleteTemporaryStaffAccountPasswordChangeCommandHandler completeTemporaryStaffAccountPasswordChangeCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(LoginStaffAccountCommandHandler loginStaffAccountCommandHandler,
                          HttpContextHelper httpContextHelper,
                          JwtGenerator jwtGenerator,
                          TaskExecutor taskExecutor,
                          RefreshStaffAccountTokenCommandHandler refreshStaffAccountTokenCommandHandler,
                          LogoutStaffAccountCommandHandler logoutStaffAccountCommandHandler,
                          CompleteTemporaryStaffAccountPasswordChangeCommandHandler completeTemporaryStaffAccountPasswordChangeCommandHandler) {
        this.loginStaffAccountCommandHandler = loginStaffAccountCommandHandler;
        this.httpContextHelper = httpContextHelper;
        this.jwtGenerator = jwtGenerator;
        this.taskExecutor = taskExecutor;
        this.refreshStaffAccountTokenCommandHandler = refreshStaffAccountTokenCommandHandler;
        this.logoutStaffAccountCommandHandler = logoutStaffAccountCommandHandler;
        this.completeTemporaryStaffAccountPasswordChangeCommandHandler = completeTemporaryStaffAccountPasswordChangeCommandHandler;
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<ResponseDto<LoginStaffAccountResponseDto>>> login(@RequestBody LoginStaffAccountRequestDto requestDto) {
        log.info("Received request to login a staff account with username: {}", requestDto.username());

        return CompletableFuture.supplyAsync(() -> {
            String ipAddress = httpContextHelper.extractIpAddress();
            var command = new LoginStaffAccountCommand(requestDto.username(), requestDto.password(), ipAddress);
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

            var responseDto = new ResponseDto<>(RefreshStaffAccountTokenResponseDto.fromCommandResponse(commandResponse), null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    @PostMapping("/logout")
    public CompletableFuture<ResponseEntity<ResponseDto<Void>>> logout() {
        log.info("Received request to logout a staff account.");

        return CompletableFuture.supplyAsync(() -> {
            String plainRefreshToken = httpContextHelper.extractRefreshTokenFromCookie();
            var command = new LogoutStaffAccountCommand(plainRefreshToken);
            logoutStaffAccountCommandHandler.handle(command);
            httpContextHelper.clearRefreshTokenCookie();

            var responseDto = new ResponseDto<Void>(null, null);
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }

    @PutMapping("/complete-temporary-password")
    public CompletableFuture<ResponseEntity<ResponseDto<CompleteTemporaryPasswordResponseDto>>> completeTemporaryPassword(
            @RequestBody CompleteTemporaryPasswordRequestDto requestDto) {
        log.info("Received request to complete temporary password change for staff account ID: {}", requestDto.id());

        return CompletableFuture.supplyAsync(() -> {
            var command = new CompleteTemporaryStaffAccountPasswordChangeCommand(
                    requestDto.id(),
                    requestDto.newPassword()
            );
            CompleteTemporaryStaffAccountPasswordChangeCommandResponse commandResponse = completeTemporaryStaffAccountPasswordChangeCommandHandler.handle(command);

            var responseDto = new ResponseDto<>(
                    CompleteTemporaryPasswordResponseDto.fromCommandResponse(commandResponse),
                    null
            );
            return ResponseEntity.ok(responseDto);
        }, taskExecutor);
    }
}
